package org.albard.dubito.app;

import java.io.IOException;
import java.net.Socket;

import org.albard.dubito.app.connection.UserConnection;
import org.albard.dubito.app.connection.UserConnectionReceiver;
import org.junit.jupiter.api.Test;

public final class UserConnectionIntegrationTest {
    @Test
    void testConnection() throws IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000);
                final UserConnection sender = UserConnection.create()) {
            sender.connect("127.0.0.1", 9000);
        }
    }
}
