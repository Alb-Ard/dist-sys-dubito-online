package org.albard.dubito;

import java.io.IOException;
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
    void testCreate() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        Assertions.assertDoesNotThrow(
                () -> PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory).close());
    }

    @Test
    void testCreateEmpty() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory);
        Assertions.assertNotNull(network);
        Assertions.assertEquals(0, network.getPeerCount());
        network.close();
    }

    @Test
    void testGetLocalPeerId() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final PeerId peerId = PeerId.createNew();
        try (final PeerNetwork network = PeerNetwork.createBound(peerId, "127.0.0.1", 9000, messengerFactory)) {
            Assertions.assertEquals(peerId, network.getLocalPeerId());
        }
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

                    try (final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                            messengerFactory)) {
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
                    }
                });
    }

    @Test
    void testConnectListener() throws IOException, InterruptedException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final PeerId remotePeerId = PeerId.createNew();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001,
                messengerFactory)) {
            final PeerExchanger remotePeerExchanger = new PeerExchanger(remotePeerId, receiver.getBindEndPoint());
            receiver.setPeerConnectedListener(t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
            try (final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                    receiver.getMessengerFactory())) {
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
            }
        }
    }

    @Test
    void testConnectToOfflinePeer() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                messengerFactory)) {
            Assertions.assertFalse(network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9001)));
            Assertions.assertEquals(0, network.getPeerCount());
        }
    }

    @Test
    void testClearAfterClose() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001,
                messengerFactory)) {
            final PeerExchanger remotePeerExchanger = new PeerExchanger(PeerId.createNew(), receiver.getBindEndPoint());
            receiver.setPeerConnectedListener(t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
            PeerNetwork network = null;
            try {
                network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                        receiver.getMessengerFactory());
            } finally {
                if (network != null) {
                    network.close();
                }
            }
            Assertions.assertEquals(0, network.getPeerCount());
        }
    }
}
