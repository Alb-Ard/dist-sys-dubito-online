package org.albard.dubito;

import static org.albard.dubito.TestUtilities.addMessageListener;
import static org.albard.dubito.TestUtilities.withNetwork;
import static org.albard.dubito.TestUtilities.withUserServer;

import java.time.Duration;
import java.util.List;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.userManagement.messages.UpdateUserMessage;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.dubito.userManagement.server.UserServer;
import org.albard.dubito.userManagement.server.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserServerTest {
    @Test
    void testCreate() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000,
                network -> Assertions.assertDoesNotThrow(() -> new UserServer(network, new UserService())));
    }

    @Test
    void testStartEmpty() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withUserServer(network, (x, server) -> {
                Assertions.assertEquals(0, server.getUserCount());
            });
        });
    }

    @Test
    void testNewUser() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client -> {
                withUserServer(network, (x, server) -> {
                    client.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(1, server.getUserCount());
                    Assertions.assertTrue(
                            server.getUsers().stream().anyMatch(u -> u.peerId().equals(client.getLocalPeerId())
                                    && u.name().equals(client.getLocalPeerId().id())));
                });
            });
        });
    }

    @Test
    void testSendUserListOnConnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client -> {
                withUserServer(network, (x, server) -> {
                    final List<UserListUpdatedMessage> updateReceived = addMessageListener(UserListUpdatedMessage.class,
                            client);
                    client.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(1, updateReceived.size());
                    Assertions.assertEquals(1, updateReceived.getFirst().getUsers().size());
                    Assertions.assertIterableEquals(server.getUsers(), updateReceived.getFirst().getUsers());
                    Assertions.assertTrue(updateReceived.getFirst().getUsers().stream()
                            .anyMatch(u -> u.peerId().equals(client.getLocalPeerId())));
                });
            });
        });
    }

    @Test
    void testSendUserListOnUpdate() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client -> {
                withUserServer(network, (x, server) -> {
                    final List<UserListUpdatedMessage> updateReceived = addMessageListener(UserListUpdatedMessage.class,
                            client);
                    client.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    client.sendMessage(new UpdateUserMessage(client.getLocalPeerId(), null, "MyName"));
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(2, updateReceived.size());
                    Assertions.assertEquals(1, updateReceived.getLast().getUsers().size());
                    Assertions.assertIterableEquals(server.getUsers(), updateReceived.getLast().getUsers());
                    Assertions.assertTrue(updateReceived.getLast().getUsers().stream()
                            .anyMatch(u -> u.peerId().equals(client.getLocalPeerId()) && u.name().equals("MyName")));
                });
            });
        });
    }

    @Test
    void testUserDisconnect() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, network -> {
            withNetwork(PeerId.createNew(), "127.0.0.1", 9001, client -> {
                withUserServer(network, (x, server) -> {
                    client.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                    Thread.sleep(Duration.ofSeconds(1));

                    client.close();
                    Thread.sleep(Duration.ofSeconds(1));

                    Assertions.assertEquals(0, server.getUserCount());
                    Assertions.assertTrue(
                            server.getUsers().stream().noneMatch(u -> u.peerId().equals(client.getLocalPeerId())));
                });
            });
        });
    }
}
