package org.albard.dubito.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.albard.dubito.app.lobby.app.views.SwingLobbyListView;
import org.albard.dubito.app.lobby.client.LobbyClient;
import org.albard.dubito.app.lobby.models.Lobby;
import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyId;
import org.albard.dubito.app.lobby.models.LobbyInfo;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;

public final class LobbyListViewer {
    private final static class ConnectionWindow extends JFrame {
        public ConnectionWindow(final Consumer<PeerEndPoint> connectionRequestListener) {
            super("Connect to Lobby Server");
            this.getRootPane().setLayout(new BoxLayout(this.getRootPane(), BoxLayout.Y_AXIS));
            final JTextField addressField = new JTextField("127.0.0.1");
            final JTextField portField = new JTextField("9000");
            this.getRootPane().add(createFieldsSection(addressField, portField));
            final JButton connectButton = new JButton("Connect");
            connectButton.addActionListener(e -> connectionRequestListener.accept(
                    PeerEndPoint.createFromValues(addressField.getText(), Integer.parseInt(portField.getText()))));
            this.getRootPane().add(connectButton);
            this.setMinimumSize(new Dimension(300, 100));
        }

        private static JComponent createFieldsSection(final JTextField addressField, final JTextField portField) {
            final JPanel section = new JPanel();
            section.setLayout(new BoxLayout(section, BoxLayout.X_AXIS));
            section.add(addressField);
            section.add(portField);
            return section;
        }
    }

    private static final class MainWindow extends JFrame {
        private static final LobbyInfo NEW_LOBBY_DEFAULT_INFO = new LobbyInfo("New Lobby", "");

        private final LobbyClient client;
        private final LobbyListPanel listPanel;
        private final CurrentLobbyPanel currentLobbyPanel;

        public MainWindow(final LobbyClient client) {
            super("List Window");
            this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
            this.client = client;
            this.listPanel = new LobbyListPanel(e -> this.client.requestNewLobby(NEW_LOBBY_DEFAULT_INFO),
                    i -> this.client.requestJoinLobby(i, ""));
            this.listPanel.setVisible(false);
            this.getContentPane().add(this.listPanel);
            this.currentLobbyPanel = new CurrentLobbyPanel(this.client.getLocalPeerId(),
                    this.client::requestLeaveCurrentLobby, this.client::requestSaveLobbyInfo);
            this.currentLobbyPanel.setVisible(false);
            this.getContentPane().add(this.currentLobbyPanel);
            this.setMinimumSize(new Dimension(640, 480));
            client.addLobbyListUpdatedListener(this.listPanel::setLobbies);
            client.addCurrentLobbyUpdatedListener(this::handleCurrentLobbyChanged);
            this.handleCurrentLobbyChanged(client.getCurrentLobby());
        }

        private void handleCurrentLobbyChanged(final Optional<Lobby> currentLobby) {
            currentLobby.ifPresentOrElse(this::showCurrentLobby, this::showLobbyList);
        }

        private void showLobbyList() {
            SwingUtilities.invokeLater(() -> {
                this.currentLobbyPanel.setVisible(false);
                this.listPanel.setVisible(true);
            });
        }

        private void showCurrentLobby(final Lobby lobby) {
            SwingUtilities.invokeLater(() -> {
                this.currentLobbyPanel.setCurrentLobby(lobby);
                this.listPanel.setVisible(false);
                this.currentLobbyPanel.setVisible(true);
            });
        }
    }

    private static final class LobbyListPanel extends JPanel {
        private final SwingLobbyListView listView = new SwingLobbyListView();
        private final JLabel noLobbiesLabel = new JLabel("No lobbies found.");

        public LobbyListPanel(final ActionListener lobbyCreateRequestListener,
                final Consumer<LobbyId> lobbySelectedListener) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(createNewLobbyButton(lobbyCreateRequestListener));
            this.add(this.listView);
            this.add(this.noLobbiesLabel);
            this.listView.addLobbySelectedListener(lobbySelectedListener);
            this.setMinimumSize(new Dimension(640, 480));
            this.setLobbies(List.of());
        }

        public void setLobbies(final List<LobbyDisplay> lobbies) {
            this.listView.setLobbies(lobbies);
            this.listView.setVisible(lobbies.size() > 0);
            this.noLobbiesLabel.setVisible(lobbies.size() <= 0);
        }

        private static JButton createNewLobbyButton(final ActionListener lobbyCreateRequestListener) {
            final JButton button = new JButton("Create Lobby");
            button.addActionListener(lobbyCreateRequestListener);
            return button;
        }
    }

    private static final class CurrentLobbyPanel extends JPanel {
        private final Runnable exitLobbyListener;
        private final PeerId localPeerId;
        private final Consumer<LobbyInfo> saveLobbyInfoListener;

        private Optional<LobbyInfo> editedLobbyInfo = Optional.empty();

        public CurrentLobbyPanel(final PeerId localPeerId, final Runnable exitLobbyListener,
                final Consumer<LobbyInfo> saveLobbyInfoListener) {
            this.localPeerId = localPeerId;
            this.exitLobbyListener = exitLobbyListener;
            this.saveLobbyInfoListener = saveLobbyInfoListener;
            this.setLayout(new BorderLayout(4, 4));
        }

        public void setCurrentLobby(final Lobby lobby) {
            this.removeAll();
            final boolean isReadOnly = !lobby.getOwner().equals(this.localPeerId);
            final JButton backButton = new JButton("< Back");
            backButton.addActionListener(e -> this.exitLobbyListener.run());
            final JTextField editableLobbyName = new JTextField(lobby.getInfo().name());
            editableLobbyName.addActionListener(e -> {
                this.setEditedLobbyInfo(lobby, i -> new LobbyInfo(editableLobbyName.getText(), i.password()));
            });
            editableLobbyName.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(final FocusEvent e) {
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    CurrentLobbyPanel.this.setEditedLobbyInfo(lobby,
                            i -> new LobbyInfo(editableLobbyName.getText(), i.password()));
                }
            });
            final JLabel readOnlyLobbyName = new JLabel(lobby.getInfo().name());
            final JButton saveInfoButton = new JButton("Save");
            saveInfoButton.addActionListener(
                    e -> this.saveLobbyInfoListener.accept(this.editedLobbyInfo.orElse(lobby.getInfo())));
            this.add(isReadOnly ? createUserTopBar(backButton, readOnlyLobbyName)
                    : createAdminTopBar(backButton, editableLobbyName, saveInfoButton), BorderLayout.NORTH);
            final JList<String> participantList = new JList<String>(
                    lobby.getParticipants().stream().map(p -> p.id()).toArray(l -> new String[l]));
            this.add(participantList);
        }

        private void setEditedLobbyInfo(final Lobby lobby, final Function<LobbyInfo, LobbyInfo> mapper) {
            this.editedLobbyInfo = this.editedLobbyInfo.or(() -> Optional.of(lobby.getInfo())).map(mapper);
        }

        private static JComponent createAdminTopBar(final JButton backButton, final JTextField editableLobbyNameField,
                final JButton saveInfoButton) {
            final JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            container.add(backButton);
            container.add(editableLobbyNameField);
            container.add(saveInfoButton);
            return container;
        }

        private static JComponent createUserTopBar(final JButton backButton, final JLabel lobbyNameLabel) {
            final JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            container.add(backButton);
            container.add(lobbyNameLabel);
            return container;
        }
    }

    private final ConnectionWindow connectionWindow;

    private Optional<MainWindow> mainWindow = Optional.empty();
    private Optional<LobbyClient> client = Optional.empty();

    public static void main(String[] args) {
        new LobbyListViewer();
    }

    private LobbyListViewer() {
        this.connectionWindow = new ConnectionWindow(this::tryConnect);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.ifPresent(c -> {
                try {
                    c.close();
                } catch (final Exception ex) {
                }
            });

            this.connectionWindow.setVisible(false);
            this.mainWindow.ifPresent(w -> w.setVisible(false));
        }));

        this.connectionWindow.setVisible(true);
    }

    private void tryConnect(final PeerEndPoint remoteEndPoint) {
        try {
            this.connectionWindow.setVisible(false);
            this.client = Optional.of(LobbyClient.createAndConnect(remoteEndPoint.getHost(), remoteEndPoint.getPort()));
            final MainWindow mainWindow = new MainWindow(this.client.get());
            mainWindow.setVisible(true);
            this.mainWindow = Optional.of(mainWindow);
        } catch (final Exception ex) {
            ex.printStackTrace();
            this.client = Optional.empty();
            this.mainWindow.ifPresent(w -> w.setVisible(false));
            this.mainWindow = Optional.empty();
            this.connectionWindow.setVisible(true);
        }
    }
}
