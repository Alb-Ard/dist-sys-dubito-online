package org.albard.dubito.lobby.app.server;

import java.util.concurrent.Semaphore;

import org.albard.dubito.lobby.server.LobbyServer;

public class LobbyServerHost {
    public static void main(final String[] args) {
        try (final LobbyServer server = LobbyServer.createBound("0.0.0.0", 9000)) {
            System.out.println("Listening on 0.0.0.0:9000");
            final Semaphore shutdownLock = new Semaphore(0);
            Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
            shutdownLock.acquire();
            System.out.println("Closing...");
            server.close();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
