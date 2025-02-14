package org.albard.dubito.app.lobby.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.lobby.messages.CreateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.app.lobby.messages.LeaveLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.LeaveLobbyMessage;
import org.albard.dubito.app.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.app.lobby.messages.LobbyLeavedMessage;
import org.albard.dubito.app.lobby.messages.LobbyListUpdatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyInfoMessage;
import org.albard.dubito.app.lobby.models.Lobby;
import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyId;
import org.albard.dubito.app.lobby.models.LobbyInfo;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;

public class LobbyServer implements Closeable {
    private final LobbyService service = new LobbyService();
    private final PeerNetwork network;

    private LobbyServer(final PeerNetwork network) {
        this.network = network;
        this.network.addMessageListener(this::handleMessage);
        this.network.setPeerConnectedListener(this::handleNewPeer);
    }

    public static LobbyServer createBound(final String bindAddress, final int bindPort) throws IOException {
        return new LobbyServer(PeerNetwork.createBound(PeerId.createNew(), bindAddress, bindPort,
                new MessengerFactory(MessageSerializer.createJson())));
    }

    public int getLobbyCount() {
        return this.service.getLobbyCount();
    }

    public List<Lobby> getLobbies() {
        return List.copyOf(this.service.getLobbies().values());
    }

    @Override
    public void close() throws IOException {
        this.network.close();
    }

    private void handleNewPeer(final PeerId id, final PeerConnection connection) {
        this.sendLobbyListTo(Set.of(id));
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
        this.service.createLobby(owner, info).getValue().match(errors -> {
            this.network
                    .sendMessage(new CreateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(owner), errors));
        }, result -> {
            this.network.sendMessage(
                    new LobbyCreatedMessage(this.network.getLocalPeerId(), Set.of(owner), result.lobby().getId()));
            this.sendLobbyUpdatedToParticipants(result.lobby());
            this.sendLobbyListTo(null);
        });
    }

    private void updateLobby(final LobbyId lobbyId, final PeerId editorId, final LobbyInfo newInfo) {
        this.service.updateLobby(lobbyId, editorId, newInfo).getValue().match(errors -> {
            this.network
                    .sendMessage(new UpdateLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(editorId), errors));
        }, result -> {
            this.sendLobbyUpdatedToParticipants(result.lobby());
            this.sendLobbyListTo(null);
        });
    }

    private void joinLobby(final LobbyId lobbyId, final PeerId joinerId, final String password) {
        this.service.joinLobby(lobbyId, joinerId, password).getValue().match(errors -> {
            this.network
                    .sendMessage(new JoinLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(joinerId), errors));
        }, result -> {
            this.network.sendMessage(new LobbyJoinedMessage(this.network.getLocalPeerId(), Set.of(joinerId), lobbyId));
            this.sendLobbyUpdatedToParticipants(result.lobby());
            this.sendLobbyListTo(null);
        });
    }

    private void leaveLobby(final LobbyId lobbyId, final PeerId leaverId) {
        this.service.leaveLobby(lobbyId, leaverId).getValue().match(errors -> {
            this.network
                    .sendMessage(new LeaveLobbyFailedMessage(this.network.getLocalPeerId(), Set.of(leaverId), errors));
        }, result -> {
            if (!result.isDeleted()) {
                this.network.sendMessage(new LobbyLeavedMessage(this.network.getLocalPeerId(), Set.of(leaverId)));
                this.sendLobbyUpdatedToParticipants(result.lobby());
            } else {
                this.network.sendMessage(
                        new LobbyLeavedMessage(this.network.getLocalPeerId(), result.lobby().getParticipants()));
            }
            this.sendLobbyListTo(null);
        });
    }

    private void sendLobbyUpdatedToParticipants(final Lobby newLobby) {
        this.network.sendMessage(
                new LobbyUpdatedMessage(this.network.getLocalPeerId(), newLobby.getParticipants(), newLobby));
    }

    private void sendLobbyListTo(final Set<PeerId> receipients) {
        this.network.sendMessage(new LobbyListUpdatedMessage(this.network.getLocalPeerId(), receipients,
                this.getLobbies().stream().map(LobbyDisplay::fromLobby).toList()));
    }
}
