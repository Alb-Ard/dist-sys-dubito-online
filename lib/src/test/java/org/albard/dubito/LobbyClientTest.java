package org.albard.dubito;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.albard.dubito.TestUtilities.withLobbyServer;
import static org.albard.dubito.TestUtilities.withNetwork;

public final class LobbyClientTest {
    @Test
    void testCreate() throws IOException {
        try (final PeerNetwork clientNetwork = PeerNetwork.createBound(PeerId.createNew(), "0.0.0.0", 9001,
                TestUtilities.createMessengerFactory())) {
            Assertions.assertDoesNotThrow(() -> new LobbyClient(clientNetwork));
        }
    }

    @Test
    void testEmptyWhenServerEmpty() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
                withLobbyServer(network, x -> {
                    final LobbyClient client = new LobbyClient(clientNetwork);
                    clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Assertions.assertEquals(0, client.getLobbyCount());
                    Assertions.assertEquals(0, client.getLobbies().size());
                    Assertions.assertFalse(client.getCurrentLobby().isPresent());
                });
            });
        });
    }

    @Test
    void testCreateLobby() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
                withLobbyServer(network, x -> {
                    final LobbyClient client = new LobbyClient(clientNetwork);
                    clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                    final LobbyInfo info = new LobbyInfo("Test lobby", "");
                    client.requestNewLobby(info);
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(1, client.getLobbyCount());
                    Assertions.assertEquals(1, client.getLobbies().size());
                    Assertions.assertTrue(client.getCurrentLobby().isPresent());
                    AssertionsUtilities.assertLobby(client.getLocalPeerId(), info,
                            client.getCurrentLobby().get().getId(), Set.of(client.getLocalPeerId()),
                            client.getCurrentLobby().get());
                });
            });
        });
    }
}
