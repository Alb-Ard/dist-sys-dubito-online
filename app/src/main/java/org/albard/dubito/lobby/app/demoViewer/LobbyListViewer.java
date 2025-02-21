package org.albard.dubito.lobby.app.demoViewer;

import java.util.Optional;

import org.albard.dubito.lobby.app.demoViewer.models.ConnectionRequestModel;
import org.albard.dubito.lobby.app.demoViewer.views.ConnectionWindow;
import org.albard.dubito.lobby.app.demoViewer.views.MainWindow;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.client.UserClient;

public final class LobbyListViewer {
    private final ConnectionRequestModel connectionRequestModel = new ConnectionRequestModel("127.0.0.1", 9000);
    private final ConnectionWindow connectionWindow;

    private Optional<MainWindow> mainWindow = Optional.empty();
    private Optional<PeerNetwork> network = Optional.empty();

    public static void main(String[] args) {
        new LobbyListViewer();
    }

    private LobbyListViewer() {
        this.connectionWindow = new ConnectionWindow(this.connectionRequestModel);
        this.connectionWindow.addConnectionRequestListener(this::tryConnect);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.closeNetwork();
            this.connectionWindow.setVisible(false);
            this.mainWindow.ifPresent(w -> w.setVisible(false));
        }));

        this.connectionWindow.setVisible(true);
    }

    private void tryConnect(final PeerEndPoint remoteEndPoint) {
        try {
            this.connectionWindow.setVisible(false);
            this.network = Optional.ofNullable(PeerNetwork.createBound(PeerId.createNew(), "0.0.0.0", 0,
                    new MessengerFactory(MessageSerializer.createJson())));
            this.network.ifPresent(network -> {
                if (!network.connectToPeer(remoteEndPoint)) {
                    System.err.println("Could not connect!");
                    this.closeNetwork();
                    return;
                }
                LobbyClient lobbyClient = new LobbyClient(network);
                UserClient userClient = new UserClient(network);
                final MainWindow mainWindow = new MainWindow(lobbyClient, userClient);
                mainWindow.setVisible(true);
                this.mainWindow = Optional.of(mainWindow);
            });
        } catch (final Exception ex) {
            ex.printStackTrace();
            this.closeNetwork();
            this.mainWindow.ifPresent(w -> w.setVisible(false));
            this.mainWindow = Optional.empty();
            this.connectionWindow.setVisible(true);
        }
    }

    private void closeNetwork() {
        this.network.ifPresent(n -> {
            try {
                n.close();
            } catch (final Exception ex2) {
            }
        });
        this.network = Optional.empty();
    }
}
