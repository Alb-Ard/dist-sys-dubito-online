package org.albard.dubito.lobby.app.demoViewer.views;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.albard.dubito.lobby.app.demoViewer.controllers.LobbyClientController;
import org.albard.dubito.lobby.app.demoViewer.models.CurrentLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.JoinProtectedLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.LobbyStateModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyInfo;

public final class MainWindow extends JFrame {
    public MainWindow(final LobbyClient client) {
        super("List Window");
        this.setMinimumSize(new Dimension(640, 480));
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        final DefaultListModel<LobbyDisplay> lobbyListModel = new DefaultListModel<>();
        final CurrentLobbyModel currentLobbyModel = new CurrentLobbyModel(client.getLocalPeerId());
        final JoinProtectedLobbyModel joinLobbyModel = new JoinProtectedLobbyModel();
        final LobbyStateModel stateModel = new LobbyStateModel();
        final LobbyClientController controller = new LobbyClientController(client, currentLobbyModel, lobbyListModel,
                joinLobbyModel, stateModel);
        final JoinProtectedLobbyView joinLobbyView = new JoinProtectedLobbyView(joinLobbyModel, stateModel);
        final LobbyListView lobbyListView = new LobbyListView(lobbyListModel, stateModel);
        final CurrentLobbyView currentLobbyView = new CurrentLobbyView(currentLobbyModel, stateModel);
        this.getContentPane().add(lobbyListView);
        this.getContentPane().add(currentLobbyView);
        this.getContentPane().add(joinLobbyView);
        lobbyListView.addCreateLobbyListener(() -> controller.createLobby(new LobbyInfo("New Lobby", "")));
        lobbyListView.addLobbySelectedListener(controller::joinLobby);
        currentLobbyView.addSaveLobbyInfoListener(controller::saveLobbyInfo);
        currentLobbyView.addExitLobbyListener(controller::leaveLobby);
        joinLobbyView.addJoinListener(controller::joinProtectedLobby);
        joinLobbyView.addCancelListener(controller::cancelJoinProtectedLobby);
        stateModel.setState(LobbyStateModel.State.IN_LIST);
    }
}