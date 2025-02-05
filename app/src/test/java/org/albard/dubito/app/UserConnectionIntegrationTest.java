package org.albard.dubito.app;

import java.io.IOException;

import org.albard.dubito.app.connection.UserConnection;
import org.albard.dubito.app.connection.UserConnectionReceiver;
import org.junit.jupiter.api.Test;

public final class UserConnectionIntegrationTest {
    @Test
    void testConnection() throws IOException {
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound("127.0.0.1", 9000);
                final UserConnection sender = UserConnection.createAndConnect("127.0.0.1", 9000)) {
        }
    }
}
