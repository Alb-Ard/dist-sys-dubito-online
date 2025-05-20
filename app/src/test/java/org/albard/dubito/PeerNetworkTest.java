package org.albard.dubito;

import java.io.IOException;

import org.albard.dubito.connection.PeerConnectionReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerIdExchanger;
import org.albard.dubito.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        final PeerNetwork network = PeerNetwork.createBound(peerId, "127.0.0.1", 9000, messengerFactory);
        Assertions.assertEquals(peerId, network.getLocalPeerId());
        network.close();
    }

    @Test
    void testConnectToOnlinePeer() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001,
                messengerFactory)) {
            final PeerIdExchanger remotePeerExchanger = new PeerIdExchanger(PeerId.createNew());
            receiver.setPeerConnectedListener(t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                    receiver.getMessengerFactory());
            Assertions.assertTrue(network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9001)));
            Assertions.assertEquals(1, network.getPeerCount());
            network.close();
        }
    }

    @Test
    void testConnectToOfflinePeer() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory);
        Assertions.assertFalse(network.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9001)));
        Assertions.assertEquals(0, network.getPeerCount());
        network.close();
    }

    @Test
    void testClearAfterClose() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001,
                messengerFactory)) {
            final PeerIdExchanger remotePeerExchanger = new PeerIdExchanger(PeerId.createNew());
            receiver.setPeerConnectedListener(t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                    receiver.getMessengerFactory());
            network.close();
            Assertions.assertEquals(0, network.getPeerCount());
        }
    }
}
