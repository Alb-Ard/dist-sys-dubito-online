package org.albard.dubito.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.albard.dubito.app.connection.UserConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionTest {
    @Test
    void testCreateAndConnect() throws IOException {
        try (final UserConnection sender = UserConnection.create();
                final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            Assertions.assertDoesNotThrow(() -> sender.connect("127.0.0.1", 9000));
        }
    }

    @Test
    void testConnectAgain() throws IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnection sender = UserConnection.create();
            sender.connect("127.0.0.1", 9000);
            sender.close();
            Assertions.assertThrows(Exception.class, () -> sender.connect("127.0.0.1", 9000));
        }
    }

    @Test
    void testDisconnect() throws UnknownHostException, IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnection sender = UserConnection.create();
            sender.connect("127.0.0.1", 9000);
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }

    @Test
    void testDisconnectAgain() throws UnknownHostException, IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnection sender = UserConnection.create();
            sender.connect("127.0.0.1", 9000);
            sender.close();
            Assertions.assertDoesNotThrow(() -> sender.close());
        }
    }

    @Test
    void testReconnect() throws UnknownHostException, IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9000)) {
            final UserConnection sender = UserConnection.create();
            sender.connect("127.0.0.1", 9000);
            sender.close();
            Assertions.assertThrows(Exception.class, () -> sender.connect("127.0.0.1", 9000));
        }
    }
}
