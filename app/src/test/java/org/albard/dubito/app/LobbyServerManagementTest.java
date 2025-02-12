package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
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
            final List<LobbyCreatedMessage> createReceived = new ArrayList<>();
            final List<LobbyUpdatedMessage> updateReceived = new ArrayList<>();
            final List<CreateLobbyFailedMessage> failReceived = new ArrayList<>();
            client.addMessageListener(m -> {
                if (m instanceof LobbyCreatedMessage createdMessage) {
                    createReceived.add(createdMessage);
                    return true;
                }
                if (m instanceof LobbyUpdatedMessage updatedMessage) {
                    updateReceived.add(updatedMessage);
                    return true;
                }
                if (m instanceof CreateLobbyFailedMessage failMessage) {
                    failReceived.add(failMessage);
                    return true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getLobbyCount());
            final Lobby lobby = server.getLobbies().get(0);
            Assertions.assertEquals(1, createReceived.size());
            final LobbyCreatedMessage createdMessage = createReceived.get(0);
            Assertions.assertEquals(lobby.getId(), createdMessage.getNewLobbyId());
            Assertions.assertEquals(1, updateReceived.size());
            final LobbyUpdatedMessage updatedMessage = updateReceived.get(0);
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), info, lobby.getId(),
                    Set.of(client.getLocalPeerId()), updatedMessage.getLobby());
            Assertions.assertEquals(0, failReceived.size(),
                    () -> "Lobby creation has failed: " + failReceived.get(0).getErrors());
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
            final List<LobbyCreatedMessage> createReceived = new ArrayList<>();
            final List<LobbyUpdatedMessage> updateReceived = new ArrayList<>();
            final List<CreateLobbyFailedMessage> failReceived = new ArrayList<>();
            client.addMessageListener(m -> {
                if (m instanceof LobbyCreatedMessage createdMessage) {
                    createReceived.add(createdMessage);
                    return true;
                }
                if (m instanceof LobbyUpdatedMessage updatedMessage) {
                    updateReceived.add(updatedMessage);
                    return true;
                }
                if (m instanceof CreateLobbyFailedMessage failMessage) {
                    failReceived.add(failMessage);
                    return true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(0, createReceived.size());
            Assertions.assertEquals(0, updateReceived.size());
            Assertions.assertEquals(0, server.getLobbyCount());
            Assertions.assertEquals(1, failReceived.size());
            final CreateLobbyFailedMessage failedMessage = failReceived.get(0);
            AssertionsUtilities.assertLobbyNameValidationErrors(name, failedMessage.getErrors());
        }
    }

    @Test
    void testFailWhenOwnerIsInLobby() throws InterruptedException, IOException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo info = new LobbyInfo("Test Lobby", "");
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            final List<LobbyCreatedMessage> createReceived = new ArrayList<>();
            final List<LobbyUpdatedMessage> updateReceived = new ArrayList<>();
            final List<CreateLobbyFailedMessage> failReceived = new ArrayList<>();
            client.addMessageListener(m -> {
                if (m instanceof LobbyCreatedMessage createdMessage) {
                    createReceived.add(createdMessage);
                    return true;
                }
                if (m instanceof LobbyUpdatedMessage updatedMessage) {
                    updateReceived.add(updatedMessage);
                    return true;
                }
                if (m instanceof CreateLobbyFailedMessage failMessage) {
                    failReceived.add(failMessage);
                    return true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, info));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getLobbyCount());
            Assertions.assertEquals(1, createReceived.size());
            Assertions.assertEquals(1, updateReceived.size());
            Assertions.assertEquals(1, failReceived.size());
            final CreateLobbyFailedMessage failedMessage = failReceived.get(0);
            Assertions.assertTrue(failedMessage.getErrors().contains("user already in a lobby"));
        }
    }

    @Test
    void testUpdateLobbyInfo() throws IOException, InterruptedException {
        try (final LobbyServer server = LobbyServer.createBound("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        new MessengerFactory(MessageSerializer.createJson()))) {
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));

            final LobbyInfo newInfo = new LobbyInfo("New test Lobby", "password");
            final List<LobbyCreatedMessage> createReceived = new ArrayList<>();
            final List<LobbyUpdatedMessage> updateReceived = new ArrayList<>();
            final List<CreateLobbyFailedMessage> failReceived = new ArrayList<>();
            client.addMessageListener(m -> {
                if (m instanceof LobbyCreatedMessage createdMessage) {
                    createReceived.add(createdMessage);
                    return true;
                }
                if (m instanceof LobbyUpdatedMessage updatedMessage) {
                    updateReceived.add(updatedMessage);
                    return true;
                }
                if (m instanceof CreateLobbyFailedMessage failMessage) {
                    failReceived.add(failMessage);
                    return true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, new LobbyInfo("Test Lobby", "")));
            Thread.sleep(Duration.ofSeconds(1));

            final LobbyId lobbyId = server.getLobbies().get(0).getId();
            client.sendMessage(new UpdateLobbyInfoMessage(client.getLocalPeerId(), null, lobbyId, newInfo));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getLobbyCount());
            final Lobby lobby = server.getLobbies().get(0);
            Assertions.assertEquals(1, createReceived.size());
            Assertions.assertEquals(1, updateReceived.size());
            Assertions.assertEquals(0, failReceived.size());
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), newInfo, lobby.getId(),
                    Set.of(client.getLocalPeerId()), updateReceived.get(0).getLobby());
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
            final boolean[] failReceived = new boolean[] { false };
            client.addMessageListener(m -> {
                if (m instanceof LobbyUpdatedMessage) {
                    Assertions.fail("Lobby was updated succesfully");
                    return true;
                }
                if (m instanceof UpdateLobbyFailedMessage updateLobbyFailedMessage) {
                    Assertions.assertEquals(1, updateLobbyFailedMessage.getErrors().size());
                    AssertionsUtilities.assertLobbyNameValidationErrors(name, updateLobbyFailedMessage.getErrors());
                    failReceived[0] = true;
                }
                return false;
            });
            client.sendMessage(new CreateLobbyMessage(client.getLocalPeerId(), null, initialInfo));
            Thread.sleep(Duration.ofSeconds(1));

            final LobbyId lobbyId = server.getLobbies().get(0).getId();
            client.sendMessage(
                    new UpdateLobbyInfoMessage(client.getLocalPeerId(), null, lobbyId, new LobbyInfo(name, "")));
            Thread.sleep(Duration.ofSeconds(1));

            final Lobby lobby = server.getLobbies().get(0);
            AssertionsUtilities.assertLobby(client.getLocalPeerId(), initialInfo, lobby.getId(),
                    Set.of(client.getLocalPeerId()), lobby);
            Assertions.assertTrue(failReceived[0]);
        }
    }
}
