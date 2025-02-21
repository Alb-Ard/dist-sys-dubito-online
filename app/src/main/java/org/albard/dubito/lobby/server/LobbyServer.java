package org.albard.dubito.lobby.server;

import java.util.List;
import java.util.Set;

import org.albard.dubito.lobby.messages.CreateLobbyFailedMessage;
import org.albard.dubito.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.lobby.messages.JoinLobbyFailedMessage;
import org.albard.dubito.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.lobby.messages.LeaveLobbyFailedMessage;
import org.albard.dubito.lobby.messages.LeaveLobbyMessage;
import org.albard.dubito.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.lobby.messages.LobbyLeavedMessage;
import org.albard.dubito.lobby.messages.LobbyListUpdatedMessage;
import org.albard.dubito.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.lobby.messages.UpdateLobbyFailedMessage;
import org.albard.dubito.lobby.messages.UpdateLobbyInfoMessage;
import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.User;
import org.albard.dubito.userManagement.UserService;

public final class LobbyServer {
    private final LobbyService service = new LobbyService();
    private final UserService peerService;
    private final PeerNetwork network;

    public LobbyServer(final PeerNetwork network, final UserService peerService) {
        this.network = network;
        this.peerService = peerService;
        this.network.addMessageListener(this::handleMessage);
        this.peerService.addPeerAddedListener(this::handleNewPeer);
    }

    public int getLobbyCount() {
        return this.service.getLobbyCount();
    }

    public List<Lobby> getLobbies() {
        return List.copyOf(this.service.getLobbies().values());
    }

    private void handleNewPeer(final User peerInfo) {
        this.sendLobbyListTo(Set.of(peerInfo.peerId()));
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