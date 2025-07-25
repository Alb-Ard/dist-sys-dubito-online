package org.albard.dubito;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.utils.Logger;
import org.albard.dubito.network.PeerGraphNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class PeerGraphNetworkTest {
    @Test
    void testCreate() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        Assertions.assertDoesNotThrow(
                () -> PeerGraphNetwork.createBound(PeerId.createNew(), "127.0.0.1", 9000, messengerFactory).close());
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4 })
    void testConnectionPeers(final int appCount) throws Exception {
        final List<Integer> appBindPorts = Stream.iterate(10001, x -> x + 1).limit(appCount).toList();
        final List<PeerId> appIds = Stream.iterate(1, x -> x + 1).map(x -> x.toString()).map(PeerId::new)
                .limit(appCount).toList();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();

        TestUtilities.withMultiCloseable(appCount,
                i -> PeerGraphNetwork.createBound(appIds.get(i), "127.0.0.1", appBindPorts.get(i), messengerFactory),
                apps -> {
                    for (int i = 0; i < appCount - 1; i++) {
                        Logger.logInfo("Connecting " + i + " to " + (i + 1));
                        Assertions.assertTrue(
                                apps.get(i).connectToPeer(PeerEndPoint.ofValues("127.0.0.1", appBindPorts.get(i + 1))));
                    }
                    Logger.logInfo("Closing");
                });
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4 })
    void testSequentialGraphConstruction(final int appCount) throws Exception {
        final List<Integer> appBindPorts = Stream.iterate(10001, x -> x + 1).limit(appCount).toList();
        final List<PeerId> appIds = Stream.iterate(1, x -> x + 1).map(x -> x.toString()).map(PeerId::new)
                .limit(appCount).toList();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();

        TestUtilities.withMultiCloseable(appCount,
                i -> PeerGraphNetwork.createBound(appIds.get(i), "127.0.0.1", appBindPorts.get(i), messengerFactory),
                apps -> {
                    for (int i = 0; i < appCount - 1; i++) {
                        apps.get(i).connectToPeer(PeerEndPoint.ofValues("127.0.0.1", appBindPorts.get(i + 1)));
                    }

                    Thread.sleep(Duration.ofMillis(appCount * 300));

                    for (int i = 0; i < appCount; i++) {
                        final PeerId appId = appIds.get(i);
                        AssertionsUtilities.assertStreamEqualsUnordered(appIds.stream().filter(x -> !appId.equals(x)),
                                apps.get(i).getPeers().keySet().stream());
                    }
                });
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4 })
    void testConcurrentGraphConstruction(final int appCount) throws Exception {
        final List<Integer> appBindPorts = Stream.iterate(10001, x -> x + 1).limit(appCount).toList();
        final List<PeerId> appIds = Stream.iterate(1, x -> x + 1).map(x -> x.toString()).map(PeerId::new)
                .limit(appCount).toList();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        TestUtilities.withMultiCloseable(appCount,
                i -> PeerGraphNetwork.createBound(appIds.get(i), "127.0.0.1", appBindPorts.get(i), messengerFactory),
                apps -> {
                    apps.stream().skip(1)
                            .map(app -> executor.submit(
                                    () -> app.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", appBindPorts.get(0)))))
                            .map(x -> {
                                try {
                                    return x.get();
                                } catch (final Exception e) {
                                    return false;
                                }
                            }).toList();

                    Thread.sleep(Duration.ofMillis(appCount * 300));

                    for (int i = 0; i < appCount; i++) {
                        final PeerId appId = appIds.get(i);
                        AssertionsUtilities.assertStreamEqualsUnordered(appIds.stream().filter(x -> !appId.equals(x)),
                                apps.get(i).getPeers().keySet().stream());
                    }
                });

    }

    @Test
    void testDisconnection() throws IOException, InterruptedException {
        final PeerId app1Id = PeerId.createNew();
        final PeerId app2Id = PeerId.createNew();
        final PeerId app3Id = PeerId.createNew();
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();

        try (final PeerNetwork app1 = PeerGraphNetwork.createBound(app1Id, "127.0.0.1", 9000, messengerFactory);
                final PeerNetwork app2 = PeerGraphNetwork.createBound(app2Id, "127.0.0.1", 9001, messengerFactory)) {

            try (final PeerNetwork app3 = PeerGraphNetwork.createBound(app3Id, "127.0.0.1", 9002, messengerFactory)) {
                app1.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9001));
                app2.connectToPeer(PeerEndPoint.ofValues("127.0.0.1", 9002));

                Thread.sleep(Duration.ofSeconds(1));
            }

            Thread.sleep(Duration.ofSeconds(1));

            Assertions.assertEquals(1, app1.getPeerCount());
            Assertions.assertTrue(app1.getPeers().keySet().contains(app2Id));

            Assertions.assertEquals(1, app2.getPeerCount());
            Assertions.assertTrue(app2.getPeers().keySet().contains(app1Id));
        }
    }
}
