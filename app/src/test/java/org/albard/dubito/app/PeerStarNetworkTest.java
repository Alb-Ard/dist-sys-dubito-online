package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;

import org.albard.dubito.app.messaging.HashMapMessageDispatcher;
import org.albard.dubito.app.messaging.MessageDispatcher;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.albard.dubito.app.network.PeerStarNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PeerStarNetworkTest {
    @Test
    void testDoubleConnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final MessengerFactory messengerFactory = new MessengerFactory(MessageSerializer.createJson());
        final MessageDispatcher app1Dispatcher = new HashMapMessageDispatcher(app1Id);
        final MessageDispatcher app2Dispatcher = new HashMapMessageDispatcher(app2Id);

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, app1Dispatcher,
                messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001,
                        app2Dispatcher,
                        messengerFactory)) {

            app1Dispatcher.start();
            app2Dispatcher.start();

            app1.start();
            app2.start();

            app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001));

            Thread.sleep(Duration.ofSeconds(3));

            Assertions.assertEquals(1, app1.getPeerCount());
            Assertions.assertArrayEquals(new PeerId[] { app2Id },
                    app1.getPeers().keySet().toArray(new PeerId[0]));
            Assertions.assertEquals(1, app2.getPeerCount());
            Assertions.assertArrayEquals(new PeerId[] { app1Id },
                    app2.getPeers().keySet().toArray(new PeerId[0]));
        }
    }

    @Test
    void testTripleConnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final PeerId app3Id = PeerId.createNew();
        final MessengerFactory messengerFactory = new MessengerFactory(MessageSerializer.createJson());
        final MessageDispatcher app1Dispatcher = new HashMapMessageDispatcher(app1Id);
        final MessageDispatcher app2Dispatcher = new HashMapMessageDispatcher(app2Id);
        final MessageDispatcher app3Dispatcher = new HashMapMessageDispatcher(app3Id);

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, app1Dispatcher,
                messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001,
                        app2Dispatcher,
                        messengerFactory);
                final PeerNetwork app3 = PeerStarNetwork.createBound(app3Id, "127.0.0.1", 9002,
                        app3Dispatcher,
                        messengerFactory)) {

            app1Dispatcher.start();
            app2Dispatcher.start();
            app3Dispatcher.start();

            app1.start();
            app2.start();
            app3.start();

            app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001));
            app2.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9002));

            Thread.sleep(Duration.ofSeconds(3));

            Assertions.assertEquals(2, app1.getPeerCount());
            Assertions.assertArrayEquals(new PeerId[] { app2Id, app3Id },
                    app1.getPeers().keySet().toArray(new PeerId[0]));
            Assertions.assertEquals(2, app2.getPeerCount());
            Assertions.assertArrayEquals(new PeerId[] { app1Id, app3Id },
                    app2.getPeers().keySet().toArray(new PeerId[0]));
            Assertions.assertEquals(2, app3.getPeerCount());
            Assertions.assertArrayEquals(new PeerId[] { app1Id, app2Id },
                    app3.getPeers().keySet().toArray(new PeerId[0]));
        }
    }
}
