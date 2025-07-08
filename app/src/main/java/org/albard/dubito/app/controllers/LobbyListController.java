package org.albard.dubito.app.controllers;

import java.util.List;
import java.util.Optional;

import javax.swing.DefaultListModel;

import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.JoinProtectedLobbyModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.mvc.ModelPropertyChangeEvent;

public final class LobbyListController {
    private final AppStateModel stateModel;
    private final JoinProtectedLobbyModel joinLobbyModel;
    private final DefaultListModel<LobbyDisplay> lobbyListModel;

    public LobbyListController(final AppStateModel stateModel, final JoinProtectedLobbyModel joinLobbyModel,
            final DefaultListModel<LobbyDisplay> lobbyListModel) {
        this.stateModel = stateModel;
        this.joinLobbyModel = joinLobbyModel;
        this.lobbyListModel = lobbyListModel;
        this.stateModel.addModelPropertyChangeListener(AppStateModel.LOBBY_CLIENT_PROPERTY, this::onLobbyClientChanged,
                null);
    }

    public void joinLobby(final LobbyDisplay lobby) {
        if (lobby.isPasswordProtected()) {
            this.joinLobbyModel.setLobbyId(lobby.id());
            this.stateModel.setState(AppStateModel.State.REQUESTING_LOBBY_PASSWORD);
        } else {
            this.stateModel.getLobbyClient().ifPresent(x -> x.requestJoinLobby(lobby.id(), ""));
        }
    }

    public void createLobby(final LobbyInfo newLobbyInfo) {
        this.stateModel.getLobbyClient().ifPresent(x -> x.requestNewLobby(newLobbyInfo));
    }

    public void joinProtectedLobby(final LobbyId id, final String password) {
        this.stateModel.getLobbyClient().ifPresent(x -> x.requestJoinLobby(id, password));
    }

    public void cancelJoinProtectedLobby() {
        this.stateModel.setState(AppStateModel.State.IN_LOBBY_LIST);
    }

    private void onLobbyClientChanged(final ModelPropertyChangeEvent<Optional<LobbyClient>> e) {
        e.getOldTypedValue().ifPresent(x -> x.removeLobbyListUpdatedListener(this::updateLobbyList));
        e.getNewTypedValue().ifPresent(x -> {
            x.addLobbyListUpdatedListener(this::updateLobbyList);
            this.updateLobbyList(x.getLobbies());
        });
    }

    private void updateLobbyList(final List<LobbyDisplay> lobbies) {
        this.lobbyListModel.clear();
        this.lobbyListModel.addAll(lobbies);
    }
}
