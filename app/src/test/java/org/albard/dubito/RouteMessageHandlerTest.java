package org.albard.dubito;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.albard.dubito.messaging.handlers.RouteMessageHandler;
import org.albard.dubito.messaging.messages.ConnectionRouteMessage;
import org.albard.dubito.messaging.messages.RouteRemovedMessage;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class RouteMessageHandlerTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> new RouteMessageHandler((i, e) -> {
        }, i -> {
        }));
    }

    @Test
    void testHandlesRouteAddedMessage() {
        final RouteMessageHandler handler = new RouteMessageHandler((i, e) -> {
        }, i -> {
        });
        Assertions.assertTrue(handler.handleMessage(new ConnectionRouteMessage(PeerId.createNew(),
                Set.of(PeerId.createNew()), TestUtilities.createEndPoint(1))));
    }

    @Test
    void testHandlesRouteRemovedMessage() {
        final RouteMessageHandler handler = new RouteMessageHandler((i, e) -> {
        }, i -> {
        });
        Assertions.assertTrue(
                handler.handleMessage(new RouteRemovedMessage(PeerId.createNew(), Set.of(PeerId.createNew()))));
    }

    @Test
    void testConnectToNewRoute() {
        final PeerId newPeerId = PeerId.createNew();
        final Map<PeerId, PeerEndPoint> receivedPeers = new HashMap<>();
        final RouteMessageHandler handler = new RouteMessageHandler((i, e) -> {
            receivedPeers.put(i, e);
        }, i -> {
        });
        final PeerEndPoint newPeerEndPoint = PeerEndPoint.ofValues("127.0.0.1", 9000);
        handler.handleMessage(new ConnectionRouteMessage(newPeerId, Set.of(), newPeerEndPoint));
        Assertions.assertEquals(1, receivedPeers.size());
        Assertions.assertEquals(receivedPeers.entrySet().stream().findFirst().get().getKey(), newPeerId);
        Assertions.assertEquals(receivedPeers.entrySet().stream().findFirst().get().getValue(), newPeerEndPoint);
    }

    @Test
    void testDisconnectFromRoute() {
        final PeerId remotePeerId = PeerId.createNew();
        final Set<PeerId> receivedPeers = new HashSet<>();
        receivedPeers.add(remotePeerId);
        final RouteMessageHandler handler = new RouteMessageHandler((i, e) -> {
        }, i -> receivedPeers.remove(i));
        handler.handleMessage(new RouteRemovedMessage(remotePeerId, Set.of()));
        Assertions.assertEquals(0, receivedPeers.size());
    }
}
