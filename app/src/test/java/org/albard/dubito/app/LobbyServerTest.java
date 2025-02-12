package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;

import org.albard.dubito.app.lobby.Lobby;
import org.albard.dubito.app.lobby.LobbyInfo;
import org.albard.dubito.app.lobby.LobbyServer;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.CreateLobbyMessage;
import org.albard.dubito.app.messaging.messages.LobbyCreatedMessage;
import org.albard.dubito.app.messaging.messages.LobbyCreationFailedMessage;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public final class LobbyServerTest {
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

    @ParameterizedTest
    @ValueSource(strings = { "password" })
    @NullAndEmptySource
    void testCreateLobby(final String password) throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            final LobbyInfo info = new LobbyInfo("Test Lobby", password);
            final boolean[] wasLobbyCreated = new boolean[] { false };
            client.addOnceMessageListener(m -> {
                if (m instanceof LobbyCreatedMessage lobbyCreatedMessage) {
                    Assertions.assertEquals(1, server.getLobbyCount());
                    final Lobby lobby = server.getLobbies().get(0);
                    Assertions.assertEquals(lobbyCreatedMessage.getNewLobbyId(), lobby.getId());
                    Assertions.assertTrue(lobby.getOwner().equals(client.getLocalPeerId()));
                    Assertions.assertTrue(lobby.getInfo().equals(info));
                    Assertions.assertEquals(1, lobby.getParticipants().size());
                    Assertions.assertTrue(lobby.getParticipants().contains(client.getLocalPeerId()));
                    wasLobbyCreated[0] = true;
                    return true;
                }
                if (m instanceof LobbyCreationFailedMessage) {
                    Assertions.fail();
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));
            Assertions.assertTrue(wasLobbyCreated[0]);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testFailCreateLobby(final String name) throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            final LobbyInfo info = new LobbyInfo(name, "");
            final boolean[] hasReceivedFailedMessage = new boolean[] { false };
            client.addOnceMessageListener(m -> {
                if (m instanceof LobbyCreatedMessage) {
                    Assertions.fail();
                    return true;
                }
                if (m instanceof LobbyCreationFailedMessage lobbyCreationFailedMessage) {
                    Assertions.assertEquals(1, lobbyCreationFailedMessage.getErrors().size());
                    if (name == null) {
                        Assertions.assertTrue(lobbyCreationFailedMessage.getErrors().contains("name can't be null"));
                    } else if (name.isBlank()) {
                        Assertions.assertTrue(lobbyCreationFailedMessage.getErrors().contains("name can't be blank"));
                    }
                    hasReceivedFailedMessage[0] = true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));
            Assertions.assertEquals(0, server.getLobbyCount());
            Assertions.assertTrue(hasReceivedFailedMessage[0]);
        }
    }

    @Test
    void testFailWhenOwnerIsInLobby() throws InterruptedException, IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            final LobbyInfo info = new LobbyInfo("Test Lobby", "");
            final boolean[] hasReceivedFailedMessage = new boolean[] { false };
            client.addOnceMessageListener(m -> {
                if (m instanceof LobbyCreationFailedMessage lobbyCreationFailedMessage) {
                    Assertions.assertEquals(1, lobbyCreationFailedMessage.getErrors().size());
                    Assertions.assertTrue(lobbyCreationFailedMessage.getErrors().contains("user already in a lobby"));
                    hasReceivedFailedMessage[0] = true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));
            Assertions.assertEquals(1, server.getLobbyCount());
            Assertions.assertTrue(hasReceivedFailedMessage[0]);
        }
    }
}
