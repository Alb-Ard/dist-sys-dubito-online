package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.albard.dubito.app.lobby.Lobby;
import org.albard.dubito.app.lobby.LobbyId;
import org.albard.dubito.app.lobby.LobbyInfo;
import org.albard.dubito.app.lobby.LobbyServer;
import org.albard.dubito.app.lobby.messages.CreateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.CreateLobbyMessage;
import org.albard.dubito.app.lobby.messages.LobbyCreatedMessage;
import org.albard.dubito.app.lobby.messages.LobbyUpdatedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyFailedMessage;
import org.albard.dubito.app.lobby.messages.UpdateLobbyInfoMessage;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.messages.ErrorGameMessageBase;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public final class LobbyServerManagementTest {
    @ParameterizedTest
    @ValueSource(strings = { "password" })
    @NullAndEmptySource
    void testCreateLobby(final String password) throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo info = new LobbyInfo("Test Lobby", password);
            final List<LobbyCreatedMessage> createReceived = TestUtilities.addMessageListener(LobbyCreatedMessage.class,
                    client);
            final List<LobbyUpdatedMessage> updateReceived = TestUtilities.addMessageListener(LobbyUpdatedMessage.class,
                    client);
            final List<ErrorGameMessageBase> failReceived = TestUtilities.addMessageListener(ErrorGameMessageBase.class,
                    client);

            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getLobbyCount());
            final Lobby lobby = server.getLobbies().getFirst();
            Assertions.assertEquals(1, createReceived.size());
            Assertions.assertEquals(lobby.getId(), createReceived.getFirst().getNewLobbyId());
            Assertions.assertEquals(1, updateReceived.size());
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), info, lobby.getId(),
                    Set.of(client.getLocalPeerId()), updateReceived.getFirst().getLobby());
            Assertions.assertEquals(0, failReceived.size());
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testFailCreateLobbyWithInvalidName(final String name) throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo info = new LobbyInfo(name, "");
            final List<LobbyCreatedMessage> createReceived = TestUtilities.addMessageListener(LobbyCreatedMessage.class,
                    client);
            final List<LobbyUpdatedMessage> updateReceived = TestUtilities.addMessageListener(LobbyUpdatedMessage.class,
                    client);
            final List<ErrorGameMessageBase> failReceived = TestUtilities.addMessageListener(ErrorGameMessageBase.class,
                    client);

            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(0, createReceived.size());
            Assertions.assertEquals(0, updateReceived.size());
            Assertions.assertEquals(0, server.getLobbyCount());
            Assertions.assertEquals(1, failReceived.size());
            Assertions.assertTrue(failReceived.getFirst() instanceof CreateLobbyFailedMessage);
            AssertionsUtilities.assertLobbyNameValidationErrors(name, failReceived.getFirst().getErrors());
        }
    }

    @Test
    void testFailWhenOwnerIsInLobby() throws InterruptedException, IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo info = new LobbyInfo("Test Lobby", "");
            final List<LobbyCreatedMessage> createReceived = TestUtilities.addMessageListener(LobbyCreatedMessage.class,
                    client);
            final List<LobbyUpdatedMessage> updateReceived = TestUtilities.addMessageListener(LobbyUpdatedMessage.class,
                    client);
            final List<ErrorGameMessageBase> failReceived = TestUtilities.addMessageListener(ErrorGameMessageBase.class,
                    client);

            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getLobbyCount());
            Assertions.assertEquals(1, createReceived.size());
            Assertions.assertEquals(1, updateReceived.size());
            Assertions.assertEquals(1, failReceived.size());
            Assertions.assertTrue(failReceived.getFirst() instanceof CreateLobbyFailedMessage);
            Assertions.assertTrue(failReceived.getFirst().getErrors().contains("user already in a lobby"));
        }
    }

    @Test
    void testUpdateLobbyInfo() throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo startingInfo = new LobbyInfo("Test Lobby", "");
            final LobbyInfo newInfo = new LobbyInfo("New test Lobby", "password");
            final List<LobbyCreatedMessage> createReceived = TestUtilities.addMessageListener(LobbyCreatedMessage.class,
                    client);
            final List<LobbyUpdatedMessage> updateReceived = TestUtilities.addMessageListener(LobbyUpdatedMessage.class,
                    client);
            final List<ErrorGameMessageBase> failReceived = TestUtilities.addMessageListener(ErrorGameMessageBase.class,
                    client);

            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, startingInfo));
            Thread.sleep(Duration.ofSeconds(1));

            final LobbyId lobbyId = server.getLobbies().getFirst().getId();
            client.sendMessage(new UpdateLobbyInfoMessage(client.getLocalPeerId(), null, lobbyId, newInfo));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getLobbyCount());
            final Lobby lobby = server.getLobbies().getFirst();
            Assertions.assertEquals(1, createReceived.size());
            Assertions.assertEquals(2, updateReceived.size());
            Assertions.assertEquals(0, failReceived.size());
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), newInfo, lobby.getId(),
                    Set.of(client.getLocalPeerId()), updateReceived.getLast().getLobby());
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testLobbyInfoUpdateFailWithInvalidName(final String name) throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo initialInfo = new LobbyInfo("Test Lobby", "password");
            final List<LobbyCreatedMessage> createReceived = TestUtilities.addMessageListener(LobbyCreatedMessage.class,
                    client);
            final List<LobbyUpdatedMessage> updateReceived = TestUtilities.addMessageListener(LobbyUpdatedMessage.class,
                    client);
            final List<ErrorGameMessageBase> failReceived = TestUtilities.addMessageListener(ErrorGameMessageBase.class,
                    client);

            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, initialInfo));
            Thread.sleep(Duration.ofSeconds(1));

            final LobbyId lobbyId = server.getLobbies().getFirst().getId();
            client.sendMessage(
                    new UpdateLobbyInfoMessage(client.getLocalPeerId(), null, lobbyId, new LobbyInfo(name, "")));
            Thread.sleep(Duration.ofSeconds(1));

            final Lobby lobby = server.getLobbies().getFirst();
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), initialInfo, lobby.getId(),
                    Set.of(client.getLocalPeerId()), lobby);
            Assertions.assertEquals(1, createReceived.size());
            Assertions.assertEquals(1, updateReceived.size());
            Assertions.assertEquals(1, failReceived.size());
            Assertions.assertTrue(failReceived.getFirst() instanceof UpdateLobbyFailedMessage);
            AssertionsUtilities.assertLobbyNameValidationErrors(name, failReceived.getFirst().getErrors());
        }
    }
}
