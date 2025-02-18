package org.albard.dubito.lobby.app.demoViewer.controllers;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.albard.dubito.lobby.app.demoViewer.models.CurrentLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.JoinProtectedLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.LobbyStateModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;

public final class LobbyClientController implements LobbyListController, LobbyManagementController {
    private final LobbyClient client;
    private final LobbyStateModel stateModel;
    private final JoinProtectedLobbyModel joinLobbyModel;

    public LobbyClientController(final LobbyClient client, final CurrentLobbyModel currentLobbyModel,
            final DefaultListModel<LobbyDisplay> lobbyListModel, final JoinProtectedLobbyModel joinLobbyModel,
            final LobbyStateModel stateModel) {
        this.client = client;
        this.joinLobbyModel = joinLobbyModel;
        this.stateModel = stateModel;
        this.client.addLobbyListUpdatedListener(l -> {
            lobbyListModel.clear();
            lobbyListModel.addAll(l);
        });
        this.client.addCurrentLobbyUpdatedListener(l -> SwingUtilities.invokeLater(() -> l.ifPresentOrElse(lobby -> {
            currentLobbyModel.setFromLobby(lobby);
            stateModel.setState(LobbyStateModel.State.IN_LOBBY);
        }, () -> {
            stateModel.setState(LobbyStateModel.State.IN_LIST);
        })));
    }

    @Override
    public void saveLobbyInfo(final LobbyInfo newInfo) {
        this.client.requestSaveLobbyInfo(newInfo);
    }

    @Override
    public void leaveLobby() {
        this.client.requestLeaveCurrentLobby();
    }

    @Override
    public void joinLobby(final LobbyDisplay lobby) {
        if (lobby.isPasswordProtected()) {
            this.joinLobbyModel.setLobbyId(lobby.id());
            this.stateModel.setState(LobbyStateModel.State.REQUESTING_PASSWORD);
        } else {
            this.client.requestJoinLobby(lobby.id(), "");
        }
    }

    @Override
    public void createLobby(final LobbyInfo newLobbyInfo) {
        this.client.requestNewLobby(newLobbyInfo);
    }

    @Override
    public void joinProtectedLobby(final LobbyId id, final String password) {
        this.client.requestJoinLobby(id, password);
    }

    @Override
    public void cancelJoinProtectedLobby() {
        this.stateModel.setState(LobbyStateModel.State.IN_LIST);
    }
}
