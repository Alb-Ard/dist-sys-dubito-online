package org.albard.dubito.lobby.app.demoViewer.controllers;

import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.albard.dubito.lobby.app.demoViewer.models.CurrentLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.CurrentUserModel;
import org.albard.dubito.lobby.app.demoViewer.models.JoinProtectedLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.LobbyStateModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.userManagement.client.UserClient;

public final class LobbyController implements LobbyListController, LobbyManagementController, CurentUserController {
    private final LobbyClient lobbyClient;
    private final LobbyStateModel stateModel;
    private final JoinProtectedLobbyModel joinLobbyModel;
    private final UserClient userClient;

    public LobbyController(final LobbyClient lobbyClient, final UserClient userClient,
            final CurrentLobbyModel currentLobbyModel, final DefaultListModel<LobbyDisplay> lobbyListModel,
            final JoinProtectedLobbyModel joinLobbyModel, final LobbyStateModel stateModel,
            final CurrentUserModel userModel) {
        this.lobbyClient = lobbyClient;
        this.userClient = userClient;
        this.joinLobbyModel = joinLobbyModel;
        this.stateModel = stateModel;
        this.lobbyClient.addLobbyListUpdatedListener(l -> {
            lobbyListModel.clear();
            lobbyListModel.addAll(l);
        });
        this.lobbyClient
                .addCurrentLobbyUpdatedListener(l -> SwingUtilities.invokeLater(() -> l.ifPresentOrElse(lobby -> {
                    currentLobbyModel.setFromLobby(lobby, this::getPeerUserName);
                    stateModel.setState(LobbyStateModel.State.IN_LOBBY);
                }, () -> {
                    stateModel.setState(LobbyStateModel.State.IN_LIST);
                })));
        this.userClient.addUserListChangedListener(u -> {
            this.lobbyClient.getCurrentLobby().ifPresent(
                    l -> SwingUtilities.invokeLater(() -> currentLobbyModel.setFromLobby(l, this::getPeerUserName)));
            userModel.setName(this.userClient.getLocalUser().name());
        });
    }

    private String getPeerUserName(final PeerId peerId) {
        return Optional.ofNullable(this.userClient.getUser(peerId)).map(user -> user.name()).orElse(peerId.id());
    }

    @Override
    public void saveLobbyInfo(final LobbyInfo newInfo) {
        this.lobbyClient.requestSaveLobbyInfo(newInfo);
    }

    @Override
    public void leaveLobby() {
        this.lobbyClient.requestLeaveCurrentLobby();
    }

    @Override
    public void joinLobby(final LobbyDisplay lobby) {
        if (lobby.isPasswordProtected()) {
            this.joinLobbyModel.setLobbyId(lobby.id());
            this.stateModel.setState(LobbyStateModel.State.REQUESTING_PASSWORD);
        } else {
            this.lobbyClient.requestJoinLobby(lobby.id(), "");
        }
    }

    @Override
    public void createLobby(final LobbyInfo newLobbyInfo) {
        this.lobbyClient.requestNewLobby(newLobbyInfo);
    }

    @Override
    public void joinProtectedLobby(final LobbyId id, final String password) {
        this.lobbyClient.requestJoinLobby(id, password);
    }

    @Override
    public void cancelJoinProtectedLobby() {
        this.stateModel.setState(LobbyStateModel.State.IN_LIST);
    }

    @Override
    public void setName(final String newName) {
        this.userClient.requestSetName(newName);
    }
}
