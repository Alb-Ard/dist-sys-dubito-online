package org.albard.dubito.lobby.app.demoViewer.controllers;

import org.albard.dubito.lobby.models.LobbyInfo;

public interface LobbyManagementController {
    void saveLobbyInfo(final LobbyInfo newInfo);

    void leaveLobby();
}
