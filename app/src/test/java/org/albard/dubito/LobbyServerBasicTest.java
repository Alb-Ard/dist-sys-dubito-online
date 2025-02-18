package org.albard.dubito;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.albard.dubito.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.lobby.messages.LobbyListUpdatedMessage;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.lobby.server.LobbyServer;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
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

    @Test
    void testSendLobbyListToNewPeers() throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client1 = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 0,
                        new MessengerFactory(MessageSerializer.createJson()));
                final PeerNetwork client2 = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 0,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            final List<LobbyListUpdatedMessage> client1UpdateReceived = TestUtilities
                    .addMessageListener(LobbyListUpdatedMessage.class, client1);
            final List<LobbyListUpdatedMessage> client2UpdateReceived = TestUtilities
                    .addMessageListener(LobbyListUpdatedMessage.class, client2);

            client1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client2.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, client1UpdateReceived.size());
            Assertions.assertEquals(0, client1UpdateReceived.getFirst().getLobbies().size());
            Assertions.assertEquals(1, client2UpdateReceived.size());
            Assertions.assertEquals(0, client2UpdateReceived.getFirst().getLobbies().size());
        }
    }

    @Test
    void testSendLobbyListOnCreate() throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 0,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            final List<LobbyListUpdatedMessage> updateReceived = TestUtilities
                    .addMessageListener(LobbyListUpdatedMessage.class, client);

            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            final LobbyInfo info = new LobbyInfo("Test Lobby", "password");
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(2, updateReceived.size());
            Assertions.assertEquals(1, updateReceived.getLast().getLobbies().size());
            Assertions.assertEquals(info.name(), updateReceived.getLast().getLobbies().getFirst().name());
            Assertions.assertEquals(info.password() != null && !info.password().isBlank(),
                    updateReceived.getLast().getLobbies().getFirst().isPasswordProtected());
            Assertions.assertEquals(1, updateReceived.getLast().getLobbies().getFirst().currentParticipantCount());
            Assertions.assertEquals(4, updateReceived.getLast().getLobbies().getFirst().maxParticipantCount());
        }
    }
}
