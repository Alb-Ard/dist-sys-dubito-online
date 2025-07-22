package org.albard.dubito.lobby.client;

import java.util.Optional;

import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyId;

public final class CurrentLobby {
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