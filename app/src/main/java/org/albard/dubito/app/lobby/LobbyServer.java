package org.albard.dubito.app.lobby;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.albard.dubito.app.Locked;
import org.albard.dubito.app.lobby.messages.CreateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.app.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.app.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyInfoMessage;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;

public class LobbyServer implements Closeable {
    private final Locked<Map<LobbyId, Lobby>> lobbies = Locked.of(new HashMap<>());
    private final PeerNetwork network;

    private LobbyServer(final PeerNetwork network) {
        this.network = network;
        this.network.addMessageListener(this::handleMessage);
    }

    public static LobbyServer createBound(final String bindAddress, final int bindPort) throws IOException {
        return new LobbyServer(PeerNetwork.createBound(PeerId.createNew(), bindAddress, bindPort,
                new MessengerFactory(MessageSerializer.createJson())));
    }

    public int getLobbyCount() {
        return this.lobbies.getValue().size();
    }

    public List<Lobby> getLobbies() {
        return List.copyOf(this.lobbies.getValue().values());
    }

    @Override
    public void close() throws IOException {
        this.network.close();
    }

    private boolean handleMessage(final GameMessage message) {
        if (message instanceof CreateLobbyMessage createLobbyMessage) {
            this.createLobby(createLobbyMessage.getSender(), createLobbyMessage.getLobbyInfo());
            return true;
        }
        if (message instanceof UpdateLobbyInfoMessage updateLobbyInfoMessage) {
            this.updateLobby(updateLobbyInfoMessage.getLobbyId(), updateLobbyInfoMessage.getSender(),
                    updateLobbyInfoMessage.getLobbyInfo());
            return true;
        }
        if (message instanceof JoinLobbyMessage joinLobbyMessage) {
            this.joinLobby(joinLobbyMessage.getLobbyId(), joinLobbyMessage.getSender(), joinLobbyMessage.getPassword());
            return true;
        }
        return false;
    }

    private void createLobby(final PeerId owner, final LobbyInfo info) {
        this.lobbies.exchange(lobbies -> {
            final List<Exception> validationErrors = info.validate();
            if (!validationErrors.isEmpty()) {
                this.network.sendMessage(new CreateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(owner),
                        validationErrors.stream().map(e -> e.getMessage()).toList()));
                return lobbies;
            }
            if (lobbies.values().stream().anyMatch(l -> l.getParticipants().contains(owner))) {
                this.network.sendMessage(new CreateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(owner),
                        List.of("user already in a lobby")));
                return lobbies;
            }
            final Lobby newLobby = new Lobby(LobbyId.createNew(), owner, info);
            System.out.println(new StringBuilder().append("Creating new lobby \"").append(info.name())
                    .append("\" owned by \"").append(owner).append(getLobbyCount()).toString());
            lobbies.put(newLobby.getId(), newLobby);
            this.network.sendMessage(
                    new LobbyCreatedMessage(this.network.getLocalPeerId(), Set.of(owner), newLobby.getId()));
            this.sendLobbyUpdatedToParticipants(newLobby);
            return lobbies;
        });
    }

    private void updateLobby(final LobbyId lobbyId, final PeerId editorId, final LobbyInfo newInfo) {
        this.lobbies.exchange(lobbies -> {
            final Lobby lobbyToEdit = lobbies.get(lobbyId);
            if (lobbyToEdit == null) {
                this.network.sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(editorId),
                        List.of("lobby not found")));
                return lobbies;
            }
            if (!lobbyToEdit.getOwner().equals(editorId)) {
                this.network.sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(editorId),
                        List.of("sender is not the lobby owner")));
                return lobbies;
            }
            final List<Exception> validationErrors = newInfo.validate();
            if (!validationErrors.isEmpty()) {
                this.network.sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(editorId),
                        validationErrors.stream().map(e -> e.getMessage()).toList()));
                return lobbies;
            }
            final Lobby newLobby = lobbyToEdit.setInfo(newInfo);
            lobbies.put(lobbyId, newLobby);
            this.sendLobbyUpdatedToParticipants(newLobby);
            return lobbies;
        });
    }

    private void joinLobby(final LobbyId lobbyId, final PeerId joinerId, final String password) {
        this.lobbies.exchange(lobbies -> {
            final Lobby lobbyToJoin = lobbies.get(lobbyId);
            if (lobbyToJoin == null) {
                this.network.sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(joinerId),
                        List.of("lobby not found")));
                return lobbies;
            }
            final String expectedPassword = lobbyToJoin.getInfo().password();
            if (expectedPassword != null && !expectedPassword.isBlank()
                    && !lobbyToJoin.getInfo().password().equals(password)) {
                this.network.sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(joinerId),
                        List.of("invalid password")));
                return lobbies;
            }
            if (lobbies.values().stream().anyMatch(l -> l.getParticipants().contains(joinerId))) {
                this.network.sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(joinerId),
                        List.of("user already in a lobby")));
                return lobbies;
            }
            final Lobby newLobby = lobbyToJoin.addParticipant(joinerId);
            lobbies.put(lobbyId, newLobby);
            this.network.sendMessage(new LobbyJoinedMessage(this.network.getLocalPeerId(), Set.of(joinerId), lobbyId));
            this.sendLobbyUpdatedToParticipants(newLobby);
            return lobbies;
        });
    }

    private void sendLobbyUpdatedToParticipants(final Lobby newLobby) {
        this.network.sendMessage(
                new LobbyUpdatedMessage(this.network.getLocalPeerId(), newLobby.getParticipants(), newLobby));
    }
}
