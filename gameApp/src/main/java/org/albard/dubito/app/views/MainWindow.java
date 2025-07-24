package org.albard.dubito.app.views;

import java.awt.Dimension;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.albard.dubito.app.controllers.ConnectionController;
import org.albard.dubito.app.controllers.CurrentUserController;
import org.albard.dubito.app.controllers.LobbyListController;
import org.albard.dubito.app.controllers.LobbyManagementController;
import org.albard.dubito.app.models.CurrentLobbyModel;
import org.albard.dubito.app.models.CurrentUserModel;
import org.albard.dubito.app.models.JoinProtectedLobbyModel;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.ConnectionRequestModel;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyInfo;

public final class MainWindow extends JFrame {
    public MainWindow(final Semaphore closeSemaphore) {
        super("Dubito Online");
        this.setMinimumSize(new Dimension(1280, 720));
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        // Create all models
        final AppStateModel stateModel = new AppStateModel();
        final ConnectionRequestModel connectionRequestModel = new ConnectionRequestModel("127.0.0.1", "9000");
        final DefaultListModel<LobbyDisplay> lobbyListModel = new DefaultListModel<>();
        final CurrentLobbyModel currentLobbyModel = new CurrentLobbyModel(stateModel);
        final JoinProtectedLobbyModel joinLobbyModel = new JoinProtectedLobbyModel();
        final CurrentUserModel userModel = new CurrentUserModel(stateModel);
        // Create controllers
        final CurrentUserController userController = new CurrentUserController(stateModel);
        final LobbyListController lobbyListController = new LobbyListController(stateModel, joinLobbyModel,
                lobbyListModel);
        final LobbyManagementController lobbyManagementController = new LobbyManagementController(stateModel,
                currentLobbyModel, gameApp -> gameApp.run(this.getContentPane()::add, closeSemaphore));
        final ConnectionController connectionController = new ConnectionController(stateModel);
        // Create views
        final MainMenuView mainMenuView = new MainMenuView(stateModel);
        final ConnectionView connectionView = new ConnectionView(stateModel, connectionRequestModel);
        final JoinProtectedLobbyView joinLobbyView = new JoinProtectedLobbyView(joinLobbyModel, stateModel);
        final LobbyListView lobbyListView = new LobbyListView(lobbyListModel, stateModel, userModel);
        final CurrentLobbyView currentLobbyView = new CurrentLobbyView(currentLobbyModel, stateModel);
        final ConnectingToGameView connectingToGameView = new ConnectingToGameView(stateModel);
        this.getContentPane().add(mainMenuView);
        this.getContentPane().add(connectionView);
        this.getContentPane().add(lobbyListView);
        this.getContentPane().add(currentLobbyView);
        this.getContentPane().add(joinLobbyView);
        this.getContentPane().add(connectingToGameView);
        // Setup listeners
        connectionView.getConnectCommand().addListener(connectionController::connectTo);
        lobbyListView.getCreateLobbyCommand()
                .addListener(() -> lobbyListController.createLobby(new LobbyInfo("New Lobby", "")));
        lobbyListView.getLobbySelectedCommand().addListener(lobbyListController::joinLobby);
        lobbyListView.getSaveUserNameCommand().addListener(userController::setName);
        currentLobbyView.getSaveLobbyInfoCommand().addListener(lobbyManagementController::saveLobbyInfo);
        currentLobbyView.getExitLobbyCommand().addListener(lobbyManagementController::leaveLobby);
        currentLobbyView.getStartGameCommand().addListener(lobbyManagementController::startGame);
        joinLobbyView.getJoinCommand().addListener(lobbyListController::joinProtectedLobby);
        joinLobbyView.getCancelCommand().addListener(lobbyListController::cancelJoinProtectedLobby);
        stateModel.setState(AppStateModel.State.IN_MAIN_MENU);
    }
}
