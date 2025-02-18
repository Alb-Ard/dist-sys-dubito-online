package org.albard.dubito.lobby.app.demoViewer.views;

import java.awt.Dimension;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.albard.dubito.lobby.app.demoViewer.models.CurrentLobbyModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyInfo;

public final class MainWindow extends JFrame {
    private static final LobbyInfo NEW_LOBBY_DEFAULT_INFO = new LobbyInfo("New Lobby", "");

    private final LobbyClient client;
    private final LobbyListView listView;
    private final CurrentLobbyView currentLobbyPanel;

    private final DefaultListModel<LobbyDisplay> lobbyListModel;
    private final CurrentLobbyModel currentLobbyModel;

    public MainWindow(final LobbyClient client) {
        super("List Window");
        this.setMinimumSize(new Dimension(640, 480));
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        this.client = client;
        this.lobbyListModel = new DefaultListModel<>();
        this.currentLobbyModel = new CurrentLobbyModel(this.client.getLocalPeerId());
        this.listView = new LobbyListView(this.lobbyListModel);
        this.listView.addCreateLobbyListener(() -> this.client.requestNewLobby(NEW_LOBBY_DEFAULT_INFO));
        this.listView.addLobbySelectedListener(i -> this.client.requestJoinLobby(i, ""));
        this.currentLobbyPanel = new CurrentLobbyView(this.currentLobbyModel);
        this.currentLobbyPanel.addSaveLobbyInfoListener(this.client::requestSaveLobbyInfo);
        this.currentLobbyPanel.addExitLobbyListener(this.client::requestLeaveCurrentLobby);
        this.currentLobbyPanel.setVisible(false);
        this.getContentPane().add(this.listView);
        this.getContentPane().add(this.currentLobbyPanel);
        client.addLobbyListUpdatedListener(l -> {
            this.lobbyListModel.clear();
            this.lobbyListModel.addAll(l);
        });
        client.addCurrentLobbyUpdatedListener(this::handleCurrentLobbyChanged);
        this.handleCurrentLobbyChanged(client.getCurrentLobby());
    }

    private void handleCurrentLobbyChanged(final Optional<Lobby> currentLobby) {
        currentLobby.ifPresentOrElse(this::showCurrentLobby, this::showLobbyList);
    }

    private void showLobbyList() {
        SwingUtilities.invokeLater(() -> {
            this.currentLobbyPanel.setVisible(false);
            this.listView.setVisible(true);
        });
    }

    private void showCurrentLobby(final Lobby lobby) {
        SwingUtilities.invokeLater(() -> {
            this.currentLobbyModel.setFromLobby(lobby);
            this.listView.setVisible(false);
            this.currentLobbyPanel.setVisible(true);
        });
    }
}