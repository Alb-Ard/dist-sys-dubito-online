package org.albard.dubito;

import static org.albard.dubito.TestUtilities.addMessageListener;
import static org.albard.dubito.TestUtilities.withLobbyServer;
import static org.albard.dubito.TestUtilities.withNetwork;
import static org.albard.dubito.TestUtilities.withUserServer;

import java.time.Duration;
import java.util.List;

import org.albard.dubito.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.lobby.messages.LobbyListUpdatedMessage;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.lobby.server.LobbyServer;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class LobbyServerBasicTest {
    @Test
    void testCreate() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withUserServer(network,
                    (userService, x) -> Assertions.assertDoesNotThrow(() -> new LobbyServer(network, userService)));
        });
    }

    @Test
    void testStartsEmpty() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withLobbyServer(network, server -> Assertions.assertEquals(0, server.getLobbyCount()));
        });
    }

    @Test
    void testSendLobbyListToNewPeers() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client1 -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, client2 -> {
                    withLobbyServer(network, x -> {
                        final List<LobbyListUpdatedMessage> client1UpdateReceived = addMessageListener(
                                LobbyListUpdatedMessage.class, client1);
                        final List<LobbyListUpdatedMessage> client2UpdateReceived = addMessageListener(
                                LobbyListUpdatedMessage.class, client2);

                        client1.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        Thread.sleep(Duration.ofSeconds(1));

                        client2.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(1, client1UpdateReceived.size());
                        Assertions.assertEquals(0, client1UpdateReceived.getFirst().getLobbies().size());
                        Assertions.assertEquals(1, client2UpdateReceived.size());
                        Assertions.assertEquals(0, client2UpdateReceived.getFirst().getLobbies().size());
                    });
                });
            });
        });
    }

    @Test
    void testSendLobbyListOnCreate() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client -> {
                withLobbyServer(network, x -> {
                    final List<LobbyListUpdatedMessage> updateReceived = addMessageListener(
                            LobbyListUpdatedMessage.class, client);

                    client.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    final LobbyInfo info = new LobbyInfo("Test Lobby", "password");
                    client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(2, updateReceived.size());
                    Assertions.assertEquals(1, updateReceived.getLast().getLobbies().size());
                    Assertions.assertEquals(info.name(), updateReceived.getLast().getLobbies().getFirst().name());
                    Assertions.assertEquals(info.password() != null && !info.password().isBlank(),
                            updateReceived.getLast().getLobbies().getFirst().isPasswordProtected());
                    Assertions.assertEquals(1,
                            updateReceived.getLast().getLobbies().getFirst().currentParticipantCount());
                    Assertions.assertEquals(4, updateReceived.getLast().getLobbies().getFirst().maxParticipantCount());
                });
            });
        });
    }
}
