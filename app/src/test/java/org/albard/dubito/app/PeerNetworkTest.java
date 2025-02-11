package org.albard.dubito.app;

import java.io.IOException;

import org.albard.dubito.app.connection.PeerConnectionReceiver;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerIdExchanger;
import org.albard.dubito.app.network.PeerNetwork;
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
    void testConnectToOnlinePeer() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001,
                messengerFactory)) {
            final PeerIdExchanger remotePeerExchanger = new PeerIdExchanger(PeerId.createNew());
            receiver.setPeerConnectedListener(t -> Assertions.assertNotNull(remotePeerExchanger.exchangeIds(t)));
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000,
                    receiver.getMessengerFactory());
            Assertions.assertTrue(network.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001)));
            Assertions.assertEquals(1, network.getPeerCount());
            network.close();
        }
    }

    @Test
    void testConnectToOfflinePeer() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory);
        Assertions.assertFalse(network.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001)));
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
