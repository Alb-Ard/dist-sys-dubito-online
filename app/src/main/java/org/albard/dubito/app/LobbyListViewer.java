package org.albard.dubito.app;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.albard.dubito.app.lobby.app.views.SwingLobbyListView;
import org.albard.dubito.app.lobby.client.LobbyClient;
import org.albard.dubito.app.lobby.models.Lobby;
import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyInfo;
import org.albard.dubito.app.network.PeerEndPoint;

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

    private static final class ListWindow extends JFrame {
        private final SwingLobbyListView listView = new SwingLobbyListView();
        private final JLabel currentLobbyNameLabel = new JLabel("<No current lobby>");

        public ListWindow(final ActionListener lobbyCreateRequestListener) {
            super("List Window");
            this.getRootPane().setLayout(new BoxLayout(this.getRootPane(), BoxLayout.Y_AXIS));
            this.getRootPane().add(createNewLobbyButton(lobbyCreateRequestListener));
            this.getRootPane().add(currentLobbyNameLabel);
            this.getRootPane().add(listView);
            this.setMinimumSize(new Dimension(640, 480));
        }

        public void setCurrentLobby(final Optional<Lobby> lobby) {
            this.currentLobbyNameLabel.setText(lobby.map(l -> l.getInfo().name()).orElse("<No current lobby>"));
        }

        public void setLobbies(final List<LobbyDisplay> lobbies) {
            this.listView.setLobbies(lobbies);
        }

        private static JButton createNewLobbyButton(final ActionListener lobbyCreateRequestListener) {
            final JButton button = new JButton("Create Lobby");
            button.addActionListener(lobbyCreateRequestListener);
            return button;
        }
    }

    private final ConnectionWindow connectionWindow;
    private final ListWindow listWindow;

    private Optional<LobbyClient> client = Optional.empty();

    public static void main(String[] args) {
        new LobbyListViewer();
    }

    private LobbyListViewer() {
        this.connectionWindow = new ConnectionWindow(this::tryConnect);
        this.listWindow = new ListWindow(e -> client.ifPresent(c -> c.requestNewLobby(new LobbyInfo("New Lobby", ""))));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.ifPresent(c -> {
                try {
                    c.close();
                } catch (final Exception ex) {
                }
            });

            this.connectionWindow.setVisible(false);
            this.listWindow.setVisible(false);
        }));

        this.connectionWindow.setVisible(true);
    }

    private void tryConnect(final PeerEndPoint remoteEndPoint) {
        try {
            this.connectionWindow.setVisible(false);
            this.client = Optional.of(LobbyClient.createAndConnect(remoteEndPoint.getHost(), remoteEndPoint.getPort()));
            this.client.ifPresent(c -> {
                c.addLobbyListUpdatedListener(l -> SwingUtilities.invokeLater(() -> this.listWindow.setLobbies(l)));
                c.addCurrentLobbyUpdatedListener(
                        l -> SwingUtilities.invokeLater(() -> this.listWindow.setCurrentLobby(l)));
            });
            this.listWindow.setLobbies(this.client.get().getLobbies());
            this.listWindow.setVisible(true);
        } catch (final Exception ex) {
            ex.printStackTrace();
            this.client = Optional.empty();
            this.connectionWindow.setVisible(true);
        }
    }
}
