package org.albard.dubito.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.albard.dubito.app.messaging.handlers.RouteMessageHandler;
import org.albard.dubito.app.messaging.messages.RouteMessage;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class RouteMessageHandlerTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> new RouteMessageHandler((i, e) -> {
        }));
    }

    @Test
    void testHandlesRouteMessage() {
        final RouteMessageHandler handler = new RouteMessageHandler((i, e) -> {
        });
        Assertions.assertTrue(handler.handleMessage(
                new RouteMessage(PeerId.createNew(), Set.of(PeerId.createNew()), TestUtilities.createMockEndPoint(1))));
    }

    @Test
    void testConnectToNewRoute() {
        final PeerId newPeerId = PeerId.createNew();
        final Map<PeerId, PeerEndPoint> receivedPeers = new HashMap<>();
        final RouteMessageHandler handler = new RouteMessageHandler((i, e) -> {
            receivedPeers.put(i, e);
        });
        final PeerEndPoint newPeerEndPoint = PeerEndPoint.createFromValues("127.0.0.1", 9000);
        handler.handleMessage(new RouteMessage(newPeerId, Set.of(), newPeerEndPoint));
        Assertions.assertEquals(1, receivedPeers.size());
        Assertions.assertEquals(receivedPeers.keySet().stream().findFirst().get(), newPeerId);
        Assertions.assertEquals(receivedPeers.get(newPeerId), newPeerEndPoint);
    }
}
