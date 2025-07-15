package org.albard.dubito.lobby.app.server;

import java.util.concurrent.Semaphore;

import org.albard.dubito.lobby.server.LobbyServer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.serialization.MessageSerializer;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.server.UserServer;
import org.albard.dubito.userManagement.server.UserService;
import org.albard.utils.Logger;

public class LobbyServerHost {
    public static void main(final String[] args) {
        final UserService peerService = new UserService();
        try (final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                new MessengerFactory(MessageSerializer.createJson()))) {
            new LobbyServer(network, peerService);
            new UserServer(network, peerService);
            Logger.logInfo("Listening on 0.0.0.0:9000");
            final Semaphore shutdownLock = new Semaphore(0);
            Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
            shutdownLock.acquire();
            Logger.logInfo("Closing...");
            network.close();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
