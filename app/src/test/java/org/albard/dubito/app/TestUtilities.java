package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public final class TestUtilities {
    private TestUtilities() {
    }

    public static ServerSocket createAndLaunchServer(final String bindAddress, final int bindPort)
            throws UnknownHostException, IOException {
        final ServerSocket server = new ServerSocket(bindPort, 4, InetAddress.getByName(bindAddress));
        Thread.ofVirtual().start(() -> {
            try {
                while (!server.isClosed()) {
                    server.accept();
                }
            } catch (final Exception e) {
            }
        });
        return server;
    }
}
