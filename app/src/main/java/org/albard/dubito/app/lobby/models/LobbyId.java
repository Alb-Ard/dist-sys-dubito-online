package org.albard.dubito.app.lobby.models;

import java.util.UUID;

public final record LobbyId(String id) {
    public static LobbyId createNew() {
        return new LobbyId(UUID.randomUUID().toString());
    }

    @Override
    public final String toString() {
        return this.id();
    }
}
