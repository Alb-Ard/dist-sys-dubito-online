package org.albard.dubito.lobby.app.demoViewer;

import java.util.Optional;

import org.albard.dubito.lobby.app.demoViewer.models.ConnectionRequestModel;
import org.albard.dubito.lobby.app.demoViewer.views.ConnectionWindow;
import org.albard.dubito.lobby.app.demoViewer.views.MainWindow;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.network.PeerEndPoint;

public final class LobbyListViewer {
    private final ConnectionRequestModel connectionRequestModel = new ConnectionRequestModel("127.0.0.1", 9000);
    private final ConnectionWindow connectionWindow;

    private Optional<MainWindow> mainWindow = Optional.empty();
    private Optional<LobbyClient> client = Optional.empty();

    public static void main(String[] args) {
        new LobbyListViewer();
    }

    private LobbyListViewer() {
        this.connectionWindow = new ConnectionWindow(this.connectionRequestModel);
        this.connectionWindow.addConnectionRequestListener(this::tryConnect);

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
