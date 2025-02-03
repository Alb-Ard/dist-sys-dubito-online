package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionTest {
    @Test
    void testCreateAndConnect() throws IOException {
        try (final UserConnectionSender sender = UserConnectionSender.create();
                final ServerSocket server = this.createAndLaunchServer("127.0.0.1", 9000)) {
            Assertions.assertDoesNotThrow(() -> sender.connect("127.0.0.1", 9000));
        }
    }

    @Test
    void testConnectAgain() throws IOException {
        try (final ServerSocket server = this.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnectionSender sender = UserConnectionSender.create();
            sender.connect("127.0.0.1", 9000);
            sender.close();
            Assertions.assertThrows(Exception.class, () -> sender.connect("127.0.0.1", 9000));
        }
    }

    @Test
    void testDisconnect() throws UnknownHostException, IOException {
        try (final ServerSocket server = this.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnectionSender sender = UserConnectionSender.create();
            sender.connect("127.0.0.1", 9000);
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }

    @Test
    void testDisconnectAgain() throws UnknownHostException, IOException {
        try (final ServerSocket server = this.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnectionSender sender = UserConnectionSender.create();
            sender.connect("127.0.0.1", 9000);
            sender.close();
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }

    @Test
    void testReconnect() throws UnknownHostException, IOException {
        try (final ServerSocket server = this.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnectionSender sender = UserConnectionSender.create();
            sender.connect("127.0.0.1", 9000);
            sender.close();
            Assertions.assertThrows(Exception.class, () -> sender.connect("127.0.0.1", 9000));
        }
    }

    private ServerSocket createAndLaunchServer(final String bindAddress, final int bindPort)
            throws UnknownHostException, IOException {
        final ServerSocket server = new ServerSocket(bindPort, 4, InetAddress.getByName(bindAddress));
        Thread.ofVirtual().start(() -> {
            try {
                while (!server.isClosed()) {
                    final Socket client = server.accept();
                }
            } catch (final Exception e) {
            }
        });
        return server;
    }
}
