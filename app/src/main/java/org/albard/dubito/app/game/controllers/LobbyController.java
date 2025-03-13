package org.albard.dubito.app.game.controllers;

import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.albard.dubito.app.game.models.CurrentLobbyModel;
import org.albard.dubito.app.game.models.CurrentUserModel;
import org.albard.dubito.app.game.models.JoinProtectedLobbyModel;
import org.albard.dubito.app.game.models.AppStateModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.userManagement.client.UserClient;

public final class LobbyController implements LobbyListController, LobbyManagementController, CurentUserController {
    private final LobbyClient lobbyClient;
    private final AppStateModel stateModel;
    private final JoinProtectedLobbyModel joinLobbyModel;
    private final UserClient userClient;

    public LobbyController(final LobbyClient lobbyClient, final UserClient userClient,
            final CurrentLobbyModel currentLobbyModel, final DefaultListModel<LobbyDisplay> lobbyListModel,
            final JoinProtectedLobbyModel joinLobbyModel, final AppStateModel stateModel,
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
                    stateModel.setState(AppStateModel.State.IN_LOBBY);
                }, () -> {
                    stateModel.setState(AppStateModel.State.IN_LOBBY_LIST);
                })));
        this.userClient.addUserListChangedListener(u -> {
            this.lobbyClient.getCurrentLobby().ifPresent(
                    l -> SwingUtilities.invokeLater(() -> currentLobbyModel.setFromLobby(l, this::getPeerUserName)));
            userModel.setName(this.userClient.getLocalUser().name());
        });
    }

    private String getPeerUserName(final Lobby lobby, final PeerId peerId) {
        return Optional.ofNullable(this.userClient.getUser(peerId))
                .map(user -> new StringBuilder().append(user.name())
                        .append(userClient.getLocalUser().peerId().equals(peerId) ? " (you)" : "")
                        .append(lobby.getOwner().equals(peerId) ? " (owner)" : "").toString())
                .orElse(peerId.id());
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
            this.stateModel.setState(AppStateModel.State.REQUESTING_LOBBY_PASSWORD);
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
        this.stateModel.setState(AppStateModel.State.IN_LOBBY_LIST);
    }

    @Override
    public void setName(final String newName) {
        this.userClient.requestSetName(newName);
    }
}
