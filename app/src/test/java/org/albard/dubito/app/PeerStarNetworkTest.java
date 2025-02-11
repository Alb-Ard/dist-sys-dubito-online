package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.albard.dubito.app.network.PeerStarNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(10)
public final class PeerStarNetworkTest {
    @Test
    void testCreate() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        Assertions.assertDoesNotThrow(
                () -> PeerStarNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory).close());
    }

    @Test
    void testConnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();

        try (final PeerNetwork app1 = PeerStarNetwork.createBound(app1Id, "127.0.0.1", 9000, messengerFactory);
                final PeerNetwork app2 = PeerStarNetwork.createBound(app2Id, "127.0.0.1", 9001, messengerFactory)) {
            Assertions.assertTrue(app1.connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", 9001)));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5 })
    void testConnectionPeers(final int appCount) throws IOException, InterruptedException {
        final List<Integer> appBindPorts = new ArrayList<>();
        final List<PeerId> appIds = new ArrayList<>();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final List<PeerNetwork> apps = new ArrayList<>();

        try {
            for (int i = 0; i < appCount; i++) {
                appBindPorts.add(9000 + i);
                appIds.add(new PeerId(Integer.toString(i)));
                apps.add(
                        PeerStarNetwork.createBound(appIds.get(i), "127.0.0.1", appBindPorts.get(i), messengerFactory));
            }

            for (int i = 0; i < appCount - 1; i++) {
                apps.get(i).connectToPeer(PeerEndPoint.createFromValues("127.0.0.1", appBindPorts.get(i + 1)));
            }

            Thread.sleep(Duration.ofSeconds(3));

            for (int i = 0; i < appCount; i++) {
                final PeerNetwork app = apps.get(i);
                final PeerId appId = appIds.get(i);
                Assertions.assertEquals(appCount - 1, app.getPeerCount());
                final List<PeerId> remoteAppIds = appIds.stream().filter(x -> !appId.equals(x)).toList();
                remoteAppIds.forEach(x -> Assertions.assertTrue(app.getPeers().keySet().contains(x)));
            }

        } finally {
            apps.forEach(a -> {
                try {
                    a.close();
                } catch (final Exception ex) {
                }
            });
        }

        // Let the system completely shutdown
        Thread.sleep(Duration.ofSeconds(1));
    }

    @Test
    void testDisconnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final PeerId app3Id = PeerId.createNew();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();

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
