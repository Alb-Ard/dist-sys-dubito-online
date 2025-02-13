package org.albard.dubito.app.lobby;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.albard.dubito.app.Locked;
import org.albard.dubito.app.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.app.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.app.lobby.messages.LobbyLeavedMessage;
import org.albard.dubito.app.lobby.messages.LobbyListUpdatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;

public final class LobbyClient implements Closeable {
    private static final class CurrentLobby {
        private final Optional<LobbyId> id;
        private final Optional<Lobby> lobby;

        private CurrentLobby(final Optional<LobbyId> id, final Optional<Lobby> lobby) {
            this.id = id;
            this.lobby = lobby;
        }

        public CurrentLobby() {
            this(Optional.empty(), Optional.empty());
        }

        public Optional<LobbyId> getId() {
            return this.id;
        }

        public Optional<Lobby> getLobby() {
            return this.lobby;
        }

        public CurrentLobby setId(final LobbyId newLobbyId) {
            return new CurrentLobby(Optional.ofNullable(newLobbyId), this.getLobby());
        }

        public CurrentLobby setLobby(final Lobby newLobby) {
            return new CurrentLobby(this.getId(), Optional.ofNullable(newLobby));
        }
    }

    private final PeerNetwork network;
    private final Locked<List<LobbyDisplay>> allLobbies = Locked.of(new ArrayList<>());
    private final Locked<CurrentLobby> currentLobby = Locked.of(new CurrentLobby());

    public LobbyClient(final PeerNetwork network) {
        this.network = network;
        this.network.addMessageListener(this::handleMessage);
    }

    public static LobbyClient createAndConnect(final String remoteAddress, final int remotePort) throws IOException {
        final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "0.0.0.0", 0,
                new MessengerFactory(MessageSerializer.createJson()));
        network.connectToPeer(PeerEndPoint.createFromValues(remoteAddress, remotePort));
        return new LobbyClient(network);
    }

    @Override
    public void close() throws IOException {
        this.network.close();
    }

    public int getLobbyCount() {
        return this.allLobbies.getValue().size();
    }

    public List<LobbyDisplay> getLobbies() {
        return List.copyOf(this.allLobbies.getValue());
    }

    public Optional<Lobby> getCurrentLobby() {
        return this.currentLobby.getValue().getLobby();
    }

    public PeerId getLocalPeerId() {
        return this.network.getLocalPeerId();
    }

    public void requestNewLobby(final LobbyInfo info) {
        this.network.sendMessage(new CreateLobbyMessage(this.getLocalPeerId(), null, info));
    }

    private boolean handleMessage(final GameMessage message) {
        if (message instanceof LobbyCreatedMessage lobbyCreatedMessage) {
            this.currentLobby.exchange(l -> l.setId(lobbyCreatedMessage.getNewLobbyId()));
            return true;
        }
        if (message instanceof LobbyJoinedMessage lobbyJoinedMessage) {
            this.currentLobby.exchange(l -> l.setId(lobbyJoinedMessage.getLobbyId()));
            return true;
        }
        if (message instanceof LobbyLeavedMessage) {
            this.currentLobby.exchange(l -> l.setId(null).setLobby(null));
            return true;
        }
        if (message instanceof LobbyUpdatedMessage lobbyUpdatedMessage) {
            this.currentLobby.exchange(l -> l.setLobby(lobbyUpdatedMessage.getLobby()));
            return true;
        }
        if (message instanceof LobbyListUpdatedMessage lobbyListUpdatedMessage) {
            this.allLobbies.exchange(l -> List.copyOf(lobbyListUpdatedMessage.getLobbies()));
            return true;
        }
        return false;
    }
}
