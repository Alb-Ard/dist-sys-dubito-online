package org.albard.dubito;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.UserService;
import org.albard.dubito.userManagement.messages.UpdateUserMessage;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.dubito.userManagement.server.UserServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserServerTest {
    @Test
    void testCreate() throws UnknownHostException, IOException {
        final UserService service = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000)) {
            Assertions.assertDoesNotThrow(() -> new UserServer(network, service));
        }
    }

    @Test
    void testStartEmpty() throws UnknownHostException, IOException {
        final UserService service = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000)) {
            final UserServer server = new UserServer(network, service);
            Assertions.assertEquals(0, server.getUserCount());
        }
    }

    @Test
    void testNewUser() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        TestUtilities.createMessengerFactory())) {
            final UserServer server = new UserServer(network, service);
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, server.getUserCount());
            Assertions.assertTrue(server.getUsers().stream().anyMatch(
                    u -> u.peerId().equals(client.getLocalPeerId()) && u.name().equals(client.getLocalPeerId().id())));
        }
    }

    @Test
    void testSendUserListOnConnect() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        TestUtilities.createMessengerFactory())) {
            final UserServer server = new UserServer(network, service);
            final List<UserListUpdatedMessage> updateReceived = TestUtilities
                    .addMessageListener(UserListUpdatedMessage.class, client);
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, updateReceived.size());
            Assertions.assertEquals(1, updateReceived.getFirst().getUsers().size());
            Assertions.assertIterableEquals(server.getUsers(), updateReceived.getFirst().getUsers());
            Assertions.assertTrue(updateReceived.getFirst().getUsers().stream()
                    .anyMatch(u -> u.peerId().equals(client.getLocalPeerId())));
        }
    }

    @Test
    void testSendUserListOnUpdate() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        TestUtilities.createMessengerFactory())) {
            final UserServer server = new UserServer(network, service);
            final List<UserListUpdatedMessage> updateReceived = TestUtilities
                    .addMessageListener(UserListUpdatedMessage.class, client);
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client.sendMessage(new UpdateUserMessage(client.getLocalPeerId(), null, "MyName"));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(2, updateReceived.size());
            Assertions.assertEquals(1, updateReceived.getLast().getUsers().size());
            Assertions.assertIterableEquals(server.getUsers(), updateReceived.getLast().getUsers());
            Assertions.assertTrue(updateReceived.getLast().getUsers().stream()
                    .anyMatch(u -> u.peerId().equals(client.getLocalPeerId()) && u.name().equals("MyName")));
        }
    }

    @Test
    void testUserDisconnect() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                        TestUtilities.createMessengerFactory())) {
            final UserServer server = new UserServer(network, service);
            client.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client.close();
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(0, server.getUserCount());
            Assertions
                    .assertTrue(server.getUsers().stream().noneMatch(u -> u.peerId().equals(client.getLocalPeerId())));
        }
    }
}
