package org.albard.dubito.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionTest {
    @Test
    void testCreateAndConnect() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            final List<PeerConnection> connections = new ArrayList<>();
            Assertions.assertDoesNotThrow(() -> connections
                    .add(PeerConnection.createAndConnect("127.0.0.1", 9001, "127.0.0.1", 9000, messengerFactory)));
            connections.forEach(c -> {
                try {
                    c.close();
                } catch (Exception ex) {
                }
            });
        }
    }

    @Test
    void testDisconnect() throws UnknownHostException, IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000);
                final PeerConnection sender = PeerConnection.createAndConnect("127.0.0.1", 0, "127.0.0.1", 9000,
                        messengerFactory)) {
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }

    @Test
    void testDisconnectAgain() throws UnknownHostException, IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000);
                final PeerConnection sender = PeerConnection.createAndConnect("127.0.0.1", 0, "127.0.0.1", 9000,
                        messengerFactory)) {
            sender.close();
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }
}
