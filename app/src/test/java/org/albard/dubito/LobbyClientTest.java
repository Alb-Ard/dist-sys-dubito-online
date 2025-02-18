package org.albard.dubito;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.lobby.server.LobbyServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class LobbyClientTest {
    @Test
    void testCreateAndConnect() throws IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000)) {
            Assertions.assertDoesNotThrow(() -> LobbyClient.createAndConnect("127.0.0.1", 9000).close());
        }
    }

    @Test
    void testEmptyWhenServerEmpty() throws IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final LobbyClient client = LobbyClient.createAndConnect("127.0.0.1", 9000)) {
            Assertions.assertEquals(0, client.getLobbyCount());
            Assertions.assertEquals(0, client.getLobbies().size());
            Assertions.assertFalse(client.getCurrentLobby().isPresent());
        }
    }

    @Test
    void testCreateLobby() throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final LobbyClient client = LobbyClient.createAndConnect("127.0.0.1", 9000)) {

            final LobbyInfo info = new LobbyInfo("Test lobby", "");
            client.requestNewLobby(info);
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, client.getLobbyCount());
            Assertions.assertEquals(1, client.getLobbies().size());
            Assertions.assertTrue(client.getCurrentLobby().isPresent());
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), info, client.getCurrentLobby().get().getId(),
                    Set.of(client.getLocalPeerId()), client.getCurrentLobby().get());
        }
    }

    @Test
    void testHasNewLobbies() throws IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final LobbyClient owner = LobbyClient.createAndConnect("127.0.0.1", 9000);
                final LobbyClient observer = LobbyClient.createAndConnect("127.0.0.1", 9000)) {
        }
    }
}
