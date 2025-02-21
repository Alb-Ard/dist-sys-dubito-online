package org.albard.dubito;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.lobby.server.LobbyServer;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class LobbyClientTest {
    @Test
    void testCreateAndConnect() throws IOException {
        final UserService peerService = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000)) {
            new LobbyServer(network, peerService);
            Assertions.assertDoesNotThrow(() -> LobbyClient.createAndConnect("127.0.0.1", 9000).close());
        }
    }

    @Test
    void testEmptyWhenServerEmpty() throws IOException {
        final UserService peerService = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000)) {
            new LobbyServer(network, peerService);
            final LobbyClient client = LobbyClient.createAndConnect("127.0.0.1", 9000);
            Assertions.assertEquals(0, client.getLobbyCount());
            Assertions.assertEquals(0, client.getLobbies().size());
            Assertions.assertFalse(client.getCurrentLobby().isPresent());
            client.close();
        }
    }

    @Test
    void testCreateLobby() throws IOException, InterruptedException {
        final UserService peerService = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000)) {
            new LobbyServer(network, peerService);
            final LobbyClient client = LobbyClient.createAndConnect("127.0.0.1", 9000);

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
}
