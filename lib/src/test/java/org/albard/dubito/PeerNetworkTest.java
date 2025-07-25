package org.albard.dubito;

import static org.albard.dubito.TestUtilities.withCloseable;
import static org.albard.dubito.TestUtilities.withNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.connection.PeerConnectionReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerExchanger;
import org.albard.dubito.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class PeerNetworkTest {
    @Test
    void testCreate() throws Exception {
        Assertions.assertDoesNotThrow(() -> withCloseable(() -> PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1",
                9000, TestUtilities.createMessengerFactory()), x -> {
                }));
        Thread.sleep(50);
    }

    @Test
    void testCreateEmpty() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9000, TestUtilities.createMessengerFactory(), network -> {
            Assertions.assertNotNull(network);
            Assertions.assertEquals(0, network.getPeerCount());
        });
    }

    @Test
    void testGetLocalPeerId() throws Exception {
        final PeerId peerId = PeerId.createNew();
        withNetwork(peerId, "127.0.0.1", 9000, network -> {
            Assertions.assertEquals(peerId, network.getLocalPeerId());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3 })
    void testConnectToOnlinePeers(final int connectionCount) throws Exception {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final List<Integer> connectionPorts = Stream.iterate(9001, i -> i + 1).limit(connectionCount).toList();

        TestUtilities.withMultiCloseable(connectionCount,
                i -> PeerConnectionReceiver.createBound("127.0.0.1", connectionPorts.get(i), messengerFactory),
                connections -> {
                    final var peerExchangers = connections.stream()
                            .map(c -> new PeerExchanger(PeerId.createNew(), c.getBindEndPoint())).toList();

                    for (int i = 0; i < connectionCount; i++) {
                        final PeerExchanger exchanger = peerExchangers.get(i);
                        connections.get(i).setPeerConnectedListener(t -> exchanger.exchangeIds(t));
                    }

                    withNetwork(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory, network -> {
                        for (int i = 0; i < connectionCount; i++) {
                            Assertions.assertTrue(
                                    network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", connectionPorts.get(i))));
                        }
                        AssertionsUtilities.assertStreamEqualsUnordered(
                                peerExchangers.stream().map(x -> x.getLocalPeerId()),
                                network.getPeers().keySet().stream());
                        AssertionsUtilities.assertStreamEqualsUnordered(
                                connections.stream().map(x -> x.getBindEndPoint()),
                                network.getPeers().values().stream().map(x -> x.getRemoteEndPoint()));
                    });
                });
        Thread.sleep(50);
    }

    @Test
    void testConnectListener() throws Exception {
        final PeerId remotePeerId = PeerId.createNew();
        withCloseable(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9001, TestUtilities.createMessengerFactory()),
                receiver -> {
                    final PeerExchanger remotePeerExchanger = new PeerExchanger(remotePeerId,
                            receiver.getBindEndPoint());
                    receiver.setPeerConnectedListener(
                            t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
                    withNetwork(PeerId.createNew(), "127.0.0.1", 9000, receiver.getMessengerFactory(), network -> {
                        final List<PeerId> receivedIds = new ArrayList<>();
                        final List<PeerConnection> receivedConnections = new ArrayList<>();
                        final List<PeerEndPoint> receivedEndPoints = new ArrayList<>();
                        network.addPeerConnectedListener((id, connection, remoteEndPoint) -> {
                            receivedIds.add(id);
                            receivedConnections.add(connection);
                            receivedEndPoints.add(remoteEndPoint);
                        });
                        network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9001));
                        Thread.sleep(100);
                        Assertions.assertEquals(1, receivedIds.size());
                        Assertions.assertEquals(remotePeerId, receivedIds.getFirst());

                        Assertions.assertEquals(1, receivedConnections.size());
                        Assertions.assertEquals(PeerEndPoint.ofValues("127.0.0.1", 9001),
                                receivedConnections.getFirst().getRemoteEndPoint());

                        Assertions.assertEquals(1, receivedEndPoints.size());
                        Assertions.assertEquals(PeerEndPoint.ofValues("127.0.0.1", 9001), receivedEndPoints.getFirst());
                    });
                });
        Thread.sleep(50);
    }

    @Test
    void testConnectToOfflinePeer() throws Exception {
        withNetwork(PeerId.createNew(), "127.0.0.1", 9001, network -> {
            Assertions.assertFalse(network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000)));
            Assertions.assertEquals(0, network.getPeerCount());
        });
    }

    @Test
    void testClearAfterClose() throws Exception {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        withCloseable(() -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, messengerFactory), receiver -> {
            final PeerExchanger remotePeerExchanger = new PeerExchanger(PeerId.createNew(), receiver.getBindEndPoint());
            receiver.setPeerConnectedListener(t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9001,
                    receiver.getMessengerFactory());
            try {
                network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9000));
                Thread.sleep(50);
            } finally {
                network.close();
            }
            Assertions.assertEquals(0, network.getPeerCount());
        });
        Thread.sleep(50);
    }
}
