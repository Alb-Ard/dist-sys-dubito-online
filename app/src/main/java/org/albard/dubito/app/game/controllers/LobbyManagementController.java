package org.albard.dubito.app.game.controllers;

import org.albard.dubito.lobby.models.LobbyInfo;

public interface LobbyManagementController {
    void saveLobbyInfo(final LobbyInfo newInfo);

    void leaveLobby();
}
