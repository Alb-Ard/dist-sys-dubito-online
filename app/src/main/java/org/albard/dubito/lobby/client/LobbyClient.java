package org.albard.dubito.lobby.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.albard.dubito.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.lobby.messages.JoinLobbyMessage;
import org.albard.dubito.lobby.messages.LeaveLobbyMessage;
import org.albard.dubito.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.lobby.messages.LobbyJoinedMessage;
import org.albard.dubito.lobby.messages.LobbyLeavedMessage;
import org.albard.dubito.lobby.messages.LobbyListUpdatedMessage;
import org.albard.dubito.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.lobby.messages.UpdateLobbyInfoMessage;
import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.utils.Locked;

public final class LobbyClient {
    private final PeerNetwork network;
    private final Locked<List<LobbyDisplay>> allLobbies = Locked.of(new ArrayList<>());
    private final Locked<CurrentLobby> currentLobby = Locked.of(new CurrentLobby());
    private final Locked<Set<Consumer<List<LobbyDisplay>>>> lobbyListUpdatedListeners = Locked.of(new HashSet<>());
    private final Locked<Set<Consumer<Optional<Lobby>>>> currentLobbyUpdatedListeners = Locked.of(new HashSet<>());

    public LobbyClient(final PeerNetwork network) {
        this.network = network;
        this.network.addMessageListener(this::handleMessage);
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

    public void addLobbyListUpdatedListener(final Consumer<List<LobbyDisplay>> listener) {
        this.lobbyListUpdatedListeners.exchange(l -> {
            l.add(listener);
            return l;
        });
    }

    public void removeLobbyListUpdatedListener(final Consumer<List<LobbyDisplay>> listener) {
        this.lobbyListUpdatedListeners.exchange(l -> {
            l.remove(listener);
            return l;
        });
    }

    public void addCurrentLobbyUpdatedListener(final Consumer<Optional<Lobby>> listener) {
        this.currentLobbyUpdatedListeners.exchange(l -> {
            l.add(listener);
            return l;
        });
    }

    public void removeCurrentLobbyUpdatedListener(final Consumer<Optional<Lobby>> listener) {
        this.currentLobbyUpdatedListeners.exchange(l -> {
            l.remove(listener);
            return l;
        });
    }

    public void requestNewLobby(final LobbyInfo info) {
        this.network.sendMessage(new CreateLobbyMessage(this.getLocalPeerId(), null, info));
    }

    public void requestJoinLobby(final LobbyId id, final String password) {
        this.network.sendMessage(new JoinLobbyMessage(this.getLocalPeerId(), null, id, password));
    }

    public void requestLeaveCurrentLobby() {
        this.getCurrentLobby().ifPresent(
                l -> this.network.sendMessage(new LeaveLobbyMessage(this.getLocalPeerId(), null, l.getId())));
    }

    public void requestSaveLobbyInfo(final LobbyInfo newLobbyInfo) {
        this.getCurrentLobby().ifPresent(l -> this.network
                .sendMessage(new UpdateLobbyInfoMessage(this.getLocalPeerId(), null, l.getId(), newLobbyInfo)));
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
            Set.copyOf(this.currentLobbyUpdatedListeners.getValue()).forEach(l -> l.accept(Optional.empty()));
            return true;
        }
        if (message instanceof LobbyUpdatedMessage lobbyUpdatedMessage) {
            this.currentLobby.exchange(l -> l.setLobby(lobbyUpdatedMessage.getLobby()));
            Set.copyOf(this.currentLobbyUpdatedListeners.getValue())
                    .forEach(l -> l.accept(Optional.of(lobbyUpdatedMessage.getLobby())));
            return true;
        }
        if (message instanceof LobbyListUpdatedMessage lobbyListUpdatedMessage) {
            this.allLobbies.exchange(l -> List.copyOf(lobbyListUpdatedMessage.getLobbies()));
            Set.copyOf(this.lobbyListUpdatedListeners.getValue())
                    .forEach(l -> l.accept(lobbyListUpdatedMessage.getLobbies()));
            return true;
        }
        return false;
    }
}
