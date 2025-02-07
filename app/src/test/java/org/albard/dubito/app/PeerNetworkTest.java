package org.albard.dubito.app;

import java.io.IOException;

import org.albard.dubito.app.connection.PeerConnectionReceiver;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerIdExchanger;
import org.albard.dubito.app.network.PeerNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PeerNetworkTest {
    @Test
    void testCreate() throws IOException {
        final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000);
        Assertions.assertNotNull(network);
        Assertions.assertEquals(0, network.getPeerCount());
        network.close();
    }

    @Test
    void testConnectToOnlinePeer() throws IOException {
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001)) {
            final PeerIdExchanger remotePeerExchanger = new PeerIdExchanger(PeerId.createNew());
            receiver.setPeerConnectedListener(t -> {
                try {
                    remotePeerExchanger.exchangeIds(t);
                } catch (final IOException e) {
                    Assertions.fail(e);
                }
            });
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000);
            receiver.start();
            Assertions.assertTrue(network.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001)));
            Assertions.assertEquals(1, network.getPeerCount());
            network.close();
        }
    }

    @Test
    void testConnectToOfflinePeer() throws IOException {
        final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000);
        Assertions.assertFalse(network.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001)));
        Assertions.assertEquals(0, network.getPeerCount());
        network.close();
    }

    @Test
    void testClearAfterClose() throws IOException {
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9001)) {
            final PeerIdExchanger remotePeerExchanger = new PeerIdExchanger(PeerId.createNew());
            receiver.setPeerConnectedListener(t -> {
                try {
                    remotePeerExchanger.exchangeIds(t);
                } catch (final IOException e) {
                    Assertions.fail(e);
                }
            });
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000);
            network.close();
            Assertions.assertEquals(0, network.getPeerCount());
        }
    }
}
