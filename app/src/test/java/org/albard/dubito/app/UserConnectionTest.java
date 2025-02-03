package org.albard.dubito.app;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public final class UserConnectionTest {
    @Test
    void testConnection() throws IOException {
        final UserConnectionRepository repository = UserConnectionRepository.createEmpty();
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000);
                final UserConnectionSender sender = UserConnectionSender.create()) {
            sender.connect("127.0.0.1", 9000);
        }
    }
}
