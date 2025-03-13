package org.albard.dubito.app.game.controllers;

import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;

public interface LobbyListController {
    void createLobby(final LobbyInfo newLobbyInfo);

    void joinLobby(final LobbyDisplay lobby);

    void joinProtectedLobby(final LobbyId id, final String password);

    void cancelJoinProtectedLobby();
}
