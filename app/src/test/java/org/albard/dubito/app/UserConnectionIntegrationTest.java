package org.albard.dubito.app;

import java.io.IOException;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.connection.PeerConnectionReceiver;
import org.junit.jupiter.api.Test;

public final class UserConnectionIntegrationTest {
    @Test
    void testConnection() throws IOException {
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000);
                final PeerConnection sender = PeerConnection.createAndConnect("127.0.0.1", 0, "127.0.0.1", 9000)) {
        }
    }
}
