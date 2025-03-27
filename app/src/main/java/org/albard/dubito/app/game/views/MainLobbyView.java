package org.albard.dubito.app.game.views;

import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import org.albard.dubito.app.game.controllers.LobbyController;
import org.albard.dubito.app.game.models.AppStateModel;
import org.albard.dubito.app.game.models.CurrentLobbyModel;
import org.albard.dubito.app.game.models.CurrentUserModel;
import org.albard.dubito.app.game.models.JoinProtectedLobbyModel;
import org.albard.dubito.app.game.models.AppStateModel.State;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.client.UserClient;
import org.albard.dubito.utils.Navigator;

import com.jgoodies.binding.beans.BeanAdapter;

public final class MainLobbyView extends JPanel {
    private final AppStateModel stateModel;
    private final Navigator<State> navigator;

    public MainLobbyView(final AppStateModel stateModel) {
        this.stateModel = stateModel;
        this.navigator = new Navigator<>(this, x -> x.toString());
        new BeanAdapter<>(this.stateModel, true).addBeanPropertyChangeListener(AppStateModel.STATE_PROPERTY, e -> {
            final State newState = (State) e.getNewValue();
            this.navigator.navigateTo(newState);
        });

    }

    public void setNetwork(final Optional<PeerNetwork> network) {
        network.ifPresentOrElse(x -> this.createView(x, this.stateModel), this::removeAll);
    }

    private void createView(final PeerNetwork network, final AppStateModel stateModel) {
        final LobbyClient lobbyClient = new LobbyClient(network);
        final UserClient userClient = new UserClient(network);
        final DefaultListModel<LobbyDisplay> lobbyListModel = new DefaultListModel<>();
        final CurrentLobbyModel currentLobbyModel = new CurrentLobbyModel(lobbyClient.getLocalPeerId());
        final JoinProtectedLobbyModel joinLobbyModel = new JoinProtectedLobbyModel();
        final CurrentUserModel userModel = new CurrentUserModel(userClient.getLocalUser().name());
        final LobbyController controller = new LobbyController(lobbyClient, userClient, currentLobbyModel,
                lobbyListModel, joinLobbyModel, stateModel, userModel);
        final JoinProtectedLobbyView joinLobbyView = new JoinProtectedLobbyView(joinLobbyModel, stateModel);
        final LobbyListView lobbyListView = new LobbyListView(lobbyListModel, stateModel, userModel);
        final CurrentLobbyView currentLobbyView = new CurrentLobbyView(currentLobbyModel, stateModel);
        this.navigator.addScreen(lobbyListView, State.IN_LOBBY_LIST);
        this.navigator.addScreen(currentLobbyView, State.IN_LOBBY);
        this.navigator.addScreen(joinLobbyView, State.REQUESTING_LOBBY_PASSWORD);
        lobbyListView.addCreateLobbyListener(() -> controller.createLobby(new LobbyInfo("New Lobby", "")));
        lobbyListView.addLobbySelectedListener(controller::joinLobby);
        lobbyListView.addSaveUserNameListener(controller::setName);
        currentLobbyView.addSaveLobbyInfoListener(controller::saveLobbyInfo);
        currentLobbyView.addExitLobbyListener(controller::leaveLobby);
        joinLobbyView.addJoinListener(controller::joinProtectedLobby);
        joinLobbyView.addCancelListener(controller::cancelJoinProtectedLobby);
    }
}
