package org.albard.dubito.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.app.connection.PeerConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionTest {
    @Test
    void testCreateAndConnect() throws IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            final List<PeerConnection> connections = new ArrayList<>();
            Assertions.assertDoesNotThrow(
                    () -> connections.add(PeerConnection.createAndConnect("127.0.0.1", 9001, "127.0.0.1", 9000)));
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
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000);
                final PeerConnection sender = PeerConnection.createAndConnect("127.0.0.1", 0, "127.0.0.1", 9000)) {
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }

    @Test
    void testDisconnectAgain() throws UnknownHostException, IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000);
                final PeerConnection sender = PeerConnection.createAndConnect("127.0.0.1", 0, "127.0.0.1", 9000)) {
            sender.close();
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }
}
