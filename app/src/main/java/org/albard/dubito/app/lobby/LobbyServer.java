package org.albard.dubito.app.lobby;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.albard.dubito.app.lobby.LobbyContainer.Result;
import org.albard.dubito.app.lobby.messages.CreateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.app.lobby.messages.LeaveLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.LeaveLobbyMessage;
import org.albard.dubito.app.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.app.lobby.messages.LobbyLeavedMessage;
import org.albard.dubito.app.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyInfoMessage;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;

public class LobbyServer implements Closeable {
    private final LobbyContainer lobbies = new LobbyContainer();
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
        return this.lobbies.getLobbyCount();
    }

    public List<Lobby> getLobbies() {
        return List.copyOf(this.lobbies.getLobbies().values());
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
        if (message instanceof LeaveLobbyMessage leaveLobbyMessage) {
            this.leaveLobby(leaveLobbyMessage.getLobbyId(), leaveLobbyMessage.getSender());
            return true;
        }
        return false;
    }

    private void createLobby(final PeerId owner, final LobbyInfo info) {
        final Result result = this.lobbies.createLobby(owner, info);
        if (result.getErrors().size() > 0) {
            this.network.sendMessage(
                    new CreateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(owner), result.getErrors()));
        } else {
            this.network.sendMessage(
                    new LobbyCreatedMessage(this.network.getLocalPeerId(), Set.of(owner), result.getLobby().getId()));
            this.sendLobbyUpdatedToParticipants(result.getLobby());
        }
    }

    private void updateLobby(final LobbyId lobbyId, final PeerId editorId, final LobbyInfo newInfo) {
        final Result result = this.lobbies.updateLobby(lobbyId, editorId, newInfo);
        if (result.getErrors().size() > 0) {
            this.network.sendMessage(
                    new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(editorId), result.getErrors()));
        } else {
            this.sendLobbyUpdatedToParticipants(result.getLobby());
        }
    }

    private void joinLobby(final LobbyId lobbyId, final PeerId joinerId, final String password) {
        final Result result = this.lobbies.joinLobby(lobbyId, joinerId, password);
        if (result.getErrors().size() > 0) {
            this.network.sendMessage(
                    new JoinLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(joinerId), result.getErrors()));
        } else {
            this.network.sendMessage(new LobbyJoinedMessage(this.network.getLocalPeerId(), Set.of(joinerId), lobbyId));
            this.sendLobbyUpdatedToParticipants(result.getLobby());
        }
    }

    private void leaveLobby(final LobbyId lobbyId, final PeerId leaverId) {
        final Result result = this.lobbies.leaveLobby(lobbyId, leaverId);
        if (result.getErrors().size() > 0) {
            this.network.sendMessage(
                    new LeaveLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(leaverId), result.getErrors()));
        } else {
            this.network.sendMessage(new LobbyLeavedMessage(this.network.getLocalPeerId(), Set.of(leaverId)));
            this.sendLobbyUpdatedToParticipants(result.getLobby());
        }
    }

    private void sendLobbyUpdatedToParticipants(final Lobby newLobby) {
        this.network.sendMessage(
                new LobbyUpdatedMessage(this.network.getLocalPeerId(), newLobby.getParticipants(), newLobby));
    }
}
