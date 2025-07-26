package org.albard.dubito;

import static org.albard.dubito.TestUtilities.addMessageListener;
import static org.albard.dubito.TestUtilities.withNetwork;
import static org.albard.dubito.TestUtilities.withUserServer;

import java.time.Duration;
import java.util.List;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.userManagement.client.UserClient;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.utils.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserClientTest {
    @Test
    void testCreate() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9001,
                clientNetwork -> Assertions.assertDoesNotThrow(() -> new UserClient(clientNetwork)));
    }

    @Test
    void testStartWithLocalUserOnly() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
            final UserClient client = new UserClient(clientNetwork);
            Assertions.assertEquals(1, client.getUserCount());
            Assertions.assertEquals(clientNetwork.getLocalPeerId(), client.getLocalUser().peerId());
        });
    }

    @Test
    void testReceiveUserListOnConnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
                withUserServer(serverNetwork, (x, y) -> {
                    final List<UserListUpdatedMessage> clientListener = addMessageListener(UserListUpdatedMessage.class,
                            clientNetwork);
                    Assertions.assertTrue(clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000)));
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(1, clientListener.size());
                });
            });
        });
    }

    @Test
    void testUserClientHasListOnConnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
                withUserServer(serverNetwork, (x, y) -> {
                    final UserClient client = new UserClient(clientNetwork);
                    Assertions.assertTrue(clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000)));
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertNotNull(client.getLocalUser());
                    Assertions.assertEquals(clientNetwork.getLocalPeerId(), client.getLocalUser().peerId());
                    Assertions.assertEquals(clientNetwork.getLocalPeerId().id(), client.getLocalUser().name());
                    Assertions.assertTrue(client.getUsers().stream()
                            .anyMatch(u -> u.peerId().equals(clientNetwork.getLocalPeerId())));
                });
            });
        });
    }

    @Test
    void testSetNameGeneratesListUpdatedMessage() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
                withUserServer(serverNetwork, (x, y) -> {
                    final UserClient client = new UserClient(clientNetwork);
                    final List<UserListUpdatedMessage> clientListener = addMessageListener(UserListUpdatedMessage.class,
                            clientNetwork);
                    clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    client.requestSetName("NewName");
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(2, clientListener.size());
                    Assertions.assertEquals(1, clientListener.getLast().getUsers().size());
                    Assertions.assertTrue(clientListener.getLast().getUsers().stream()
                            .anyMatch(user -> user.peerId().equals(clientNetwork.getLocalPeerId())
                                    && user.name().equals("NewName")));
                });
            });
        });
    }

    @Test
    void testSetNameUpdatesUserClient() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, clientNetwork -> {
                withUserServer(serverNetwork, (x, y) -> {
                    final UserClient client = new UserClient(clientNetwork);
                    clientNetwork.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    client.requestSetName("NewName");
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(1, client.getUserCount());
                    Assertions.assertEquals("NewName", client.getLocalUser().name());
                });
            });
        });
    }

    @Test
    void testReceiveListUpdatedOnOtherClientConnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client1Network -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, client2Network -> {
                    withUserServer(serverNetwork, (x, y) -> {
                        final var client1Listener = addMessageListener(UserListUpdatedMessage.class, client1Network,
                                z -> Logger.logInfo(client1Network.getLocalPeerId() + ": Received List"));
                        final var client2Listener = addMessageListener(UserListUpdatedMessage.class, client2Network,
                                z -> Logger.logInfo(client2Network.getLocalPeerId() + ": Received List"));
                        client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        Thread.sleep(Duration.ofMillis(500));

                        Assertions.assertEquals(2, client1Listener.size());
                        Assertions.assertEquals(2, client1Listener.getLast().getUsers().size());

                        Assertions.assertEquals(1, client2Listener.size());
                        Assertions.assertEquals(2, client2Listener.getLast().getUsers().size());
                    });
                });
            });
        });
    }

    @Test
    void testUserClientHasUpdatedListOnOtherClientConnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client1Network -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, client2Network -> {
                    withUserServer(serverNetwork, (x, y) -> {
                        final UserClient client1 = new UserClient(client1Network);
                        final UserClient client2 = new UserClient(client2Network);
                        client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));

                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(2, client1.getUserCount());
                        Assertions.assertNotNull(client1.getUser(client2.getLocalUser().peerId()));
                        Assertions.assertTrue(client1.getUsers().stream()
                                .anyMatch(u -> u.peerId().equals(client2.getLocalUser().peerId())));

                        Assertions.assertEquals(2, client2.getUserCount());
                        Assertions.assertNotNull(client2.getUser(client1.getLocalUser().peerId()));
                        Assertions.assertTrue(client2.getUsers().stream()
                                .anyMatch(u -> u.peerId().equals(client1.getLocalUser().peerId())));
                    });
                });
            });
        });
    }

    @Test
    void testReceiveListUpdatedOnOtherClientDisconnect() throws Exception {

        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client1Network -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, client2Network -> {
                    withUserServer(serverNetwork, (x, y) -> {
                        final var client1Listener = addMessageListener(UserListUpdatedMessage.class, client1Network);
                        client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        Thread.sleep(Duration.ofSeconds(1));

                        client2Network.close();
                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(3, client1Listener.size());
                        Assertions.assertEquals(1, client1Listener.getLast().getUsers().size());
                        Assertions.assertTrue(client1Listener.getLast().getUsers().stream()
                                .anyMatch(user -> user.peerId().equals(client1Network.getLocalPeerId())
                                        && user.name().equals(client1Network.getLocalPeerId().id())));
                    });
                });
            });
        });
    }

    @Test
    void testUserClientHasUdpatedListOnOtherClientDisconnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, serverNetwork -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client1Network -> {
                withNetwork(PeerId.createNew(), "127.0.0.1", 9002, client2Network -> {
                    withUserServer(serverNetwork, (x, y) -> {
                        final UserClient client1 = new UserClient(client1Network);
                        final UserClient client2 = new UserClient(client2Network);
                        client1Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        client2Network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                        Thread.sleep(Duration.ofSeconds(1));

                        client2Network.close();
                        Thread.sleep(Duration.ofSeconds(1));

                        Assertions.assertEquals(1, client1.getUserCount());
                        Assertions.assertTrue(client1.getUsers().stream()
                                .noneMatch(u -> u.peerId().equals(client2.getLocalUser().peerId())));
                    });
                });
            });
        });
    }
}
