package org.albard.dubito;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.client.UserClient;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.dubito.userManagement.server.UserServer;
import org.albard.dubito.userManagement.server.UserService;
import org.albard.utils.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserClientTest {
    @Test
    void testCreate() throws UnknownHostException, IOException {
        try (final PeerNetwork clientNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001)) {
            Assertions.assertDoesNotThrow(() -> new UserClient(clientNetwork));
        }
    }

    @Test
    void testStartWithLocalUserOnly() throws UnknownHostException, IOException {
        try (final PeerNetwork clientNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001)) {
            final UserClient client = new UserClient(clientNetwork);
            Assertions.assertEquals(1, client.getUserCount());
            Assertions.assertEquals(clientNetwork.getLocalPeerId(), client.getLocalUser().peerId());
        }
    }

    @Test
    void testReceiveUserListOnConnect() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork clientNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001)) {
            new UserServer(serverNetwork, service);
            final List<UserListUpdatedMessage> clientListener = TestUtilities
                    .addMessageListener(UserListUpdatedMessage.class, clientNetwork);
            Assertions.assertTrue(clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000)));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, clientListener.size());
        }
    }

    @Test
    void testUserClientHasListOnConnect() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork clientNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001)) {
            new UserServer(serverNetwork, service);
            final UserClient client = new UserClient(clientNetwork);
            Assertions.assertTrue(clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000)));
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertNotNull(client.getLocalUser());
            Assertions.assertEquals(clientNetwork.getLocalPeerId(), client.getLocalUser().peerId());
            Assertions.assertEquals(clientNetwork.getLocalPeerId().id(), client.getLocalUser().name());
            Assertions.assertTrue(
                    client.getUsers().stream().anyMatch(u -> u.peerId().equals(clientNetwork.getLocalPeerId())));
        }
    }

    @Test
    void testSetNameGeneratesListUpdatedMessage() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork clientNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001)) {
            new UserServer(serverNetwork, service);
            final UserClient client = new UserClient(clientNetwork);
            final List<UserListUpdatedMessage> clientListener = TestUtilities
                    .addMessageListener(UserListUpdatedMessage.class, clientNetwork);
            clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client.requestSetName("NewName");
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(2, clientListener.size());
            Assertions.assertEquals(1, clientListener.getLast().getUsers().size());
            Assertions.assertTrue(clientListener.getLast().getUsers().stream()
                    .anyMatch(x -> x.peerId().equals(clientNetwork.getLocalPeerId()) && x.name().equals("NewName")));
        }
    }

    @Test
    void testSetNameUpdatesUserClient() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork clientNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001)) {
            new UserServer(serverNetwork, service);
            final UserClient client = new UserClient(clientNetwork);
            clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client.requestSetName("NewName");
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, client.getUserCount());
            Assertions.assertEquals("NewName", client.getLocalUser().name());
        }
    }

    @Test
    void testReceiveListUpdatedOnOtherClientConnect() throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client1Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 0);
                final PeerNetwork client2Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 0)) {
            new UserServer(serverNetwork, service);
            final var client1Listener = TestUtilities.addMessageListener(UserListUpdatedMessage.class, client1Network,
                    x -> Logger.logInfo(client1Network.getLocalPeerId() + ": Received List"));
            final var client2Listener = TestUtilities.addMessageListener(UserListUpdatedMessage.class, client2Network,
                    x -> Logger.logInfo(client2Network.getLocalPeerId() + ": Received List"));
            client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

            Thread.sleep(Duration.ofMillis(500));

            Assertions.assertEquals(2, client1Listener.size());
            Assertions.assertEquals(2, client1Listener.getLast().getUsers().size());

            Assertions.assertEquals(1, client2Listener.size());
            Assertions.assertEquals(2, client2Listener.getLast().getUsers().size());
        }
    }

    @Test
    void testUserClientHasUpdatedListOnOtherClientConnect()
            throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client1Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 0);
                final PeerNetwork client2Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 0)) {
            new UserServer(serverNetwork, service);
            final UserClient client1 = new UserClient(client1Network);
            final UserClient client2 = new UserClient(client2Network);
            client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(2, client1.getUserCount());
            Assertions.assertNotNull(client1.getUser(client2.getLocalUser().peerId()));
            Assertions.assertTrue(
                    client1.getUsers().stream().anyMatch(u -> u.peerId().equals(client2.getLocalUser().peerId())));

            Assertions.assertEquals(2, client2.getUserCount());
            Assertions.assertNotNull(client2.getUser(client1.getLocalUser().peerId()));
            Assertions.assertTrue(
                    client2.getUsers().stream().anyMatch(u -> u.peerId().equals(client1.getLocalUser().peerId())));
        }
    }

    @Test
    void testReceiveListUpdatedOnOtherClientDisconnect()
            throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client1Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001);
                final PeerNetwork client2Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9002)) {
            new UserServer(serverNetwork, service);
            final var client1Listener = TestUtilities.addMessageListener(UserListUpdatedMessage.class, client1Network);
            client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client2Network.close();
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(3, client1Listener.size());
            Assertions.assertEquals(1, client1Listener.getLast().getUsers().size());
            Assertions.assertTrue(client1Listener.getLast().getUsers().stream()
                    .anyMatch(x -> x.peerId().equals(client1Network.getLocalPeerId())
                            && x.name().equals(client1Network.getLocalPeerId().id())));
        }
    }

    @Test
    void testUserClientHasUdpatedListOnOtherClientDisconnect()
            throws UnknownHostException, IOException, InterruptedException {
        final UserService service = new UserService();
        try (final PeerNetwork serverNetwork = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9000);
                final PeerNetwork client1Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9001);
                final PeerNetwork client2Network = TestUtilities.createAndLaunchServerNetwork("127.0.0.1", 9002)) {
            new UserServer(serverNetwork, service);
            final UserClient client1 = new UserClient(client1Network);
            final UserClient client2 = new UserClient(client2Network);
            client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
            Thread.sleep(Duration.ofSeconds(1));

            client2Network.close();
            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, client1.getUserCount());
            Assertions.assertTrue(
                    client1.getUsers().stream().noneMatch(u -> u.peerId().equals(client2.getLocalUser().peerId())));
        }
    }
}
