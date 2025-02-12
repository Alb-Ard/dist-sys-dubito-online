package org.albard.dubito.app;

import java.io.IOException;

import org.albard.dubito.app.lobby.LobbyServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class LobbyServerBasicTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> LobbyServer.createBound("127.0.0.1", 9000).close());
    }

    @Test
    void testStartsEmpty() throws IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000)) {
            Assertions.assertEquals(0, server.getLobbyCount());
        }
    }
}
