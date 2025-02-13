package org.albard.dubito.app.lobby.app.views;

import java.util.List;
import java.util.function.Consumer;

import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyId;

public interface LobbyListView {
    public void setLobbies(final List<LobbyDisplay> lobbies);

    public void addLobbySelectedListener(final Consumer<LobbyId> listener);

    public void removeLobbySelectedListener(final Consumer<LobbyId> listener);
}
