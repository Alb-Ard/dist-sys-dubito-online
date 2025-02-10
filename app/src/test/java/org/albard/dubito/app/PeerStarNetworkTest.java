package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;

import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.albard.dubito.app.network.PeerStarNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class PeerStarNetworkTest {
    @Test
    @Order(1)
    void testCreate() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        Assertions.assertDoesNotThrow(
                () -> PeerStarNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory).close());
    }

    @Test
    @Order(3)
    void testConnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001, messengerFactory)) {
            Assertions.assertTrue(app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001)));
        }
    }

    @Test
    @Order(4)
    void testDoubleConnectionPeers() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final MessengerFactory messengerFactory = new MessengerFactory(MessageSerializer.createJson());

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001, messengerFactory)) {

            app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001));

            Thread.sleep(Duration.ofSeconds(3));

            Assertions.assertEquals(1, app1.getPeerCount());
            Assertions.assertTrue(app1.getPeers().keySet().contains(app2Id));

            Assertions.assertEquals(1, app2.getPeerCount());
            Assertions.assertTrue(app2.getPeers().keySet().contains(app1Id));
        }
    }

    @Test
    @Order(5)
    void testTripleConnectionPeers() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final PeerId app3Id = PeerId.createNew();
        final MessengerFactory messengerFactory = new MessengerFactory(MessageSerializer.createJson());

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001, messengerFactory);
                final PeerNetwork app3 = PeerStarNetwork.createBound(app3Id, "127.0.0.1", 9002, messengerFactory)) {

            app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001));
            app2.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9002));

            Thread.sleep(Duration.ofSeconds(3));

            Assertions.assertEquals(2, app1.getPeerCount());
            Assertions.assertTrue(app1.getPeers().keySet().contains(app2Id));
            Assertions.assertTrue(app1.getPeers().keySet().contains(app3Id));

            Assertions.assertEquals(2, app2.getPeerCount());
            Assertions.assertTrue(app2.getPeers().keySet().contains(app1Id));
            Assertions.assertTrue(app2.getPeers().keySet().contains(app3Id));

            Assertions.assertEquals(2, app3.getPeerCount());
            Assertions.assertTrue(app3.getPeers().keySet().contains(app1Id));
            Assertions.assertTrue(app3.getPeers().keySet().contains(app2Id));
        }
    }

    @Test
    @Order(6)
    void testDisconnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final PeerId app3Id = PeerId.createNew();
        final MessengerFactory messengerFactory = new MessengerFactory(MessageSerializer.createJson());

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001, messengerFactory)) {

            final PeerNetwork app3 = PeerStarNetwork.createBound(app3Id, "127.0.0.1", 9002, messengerFactory);

            app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001));
            app2.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9002));

            Thread.sleep(Duration.ofSeconds(3));

            app3.close();

            Thread.sleep(Duration.ofSeconds(3));

            Assertions.assertEquals(1, app1.getPeerCount());
            Assertions.assertTrue(app1.getPeers().keySet().contains(app2Id));

            Assertions.assertEquals(1, app2.getPeerCount());
            Assertions.assertTrue(app2.getPeers().keySet().contains(app1Id));
        }
    }
}
