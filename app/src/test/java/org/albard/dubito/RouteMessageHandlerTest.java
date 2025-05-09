package org.albard.dubito;

import java.util.HashSet;
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
        Assertions.assertDoesNotThrow(() -> new RouteMessageHandler(e -> {
        }, i -> {
        }));
    }

    @Test
    void testHandlesRouteAddedMessage() {
        final RouteMessageHandler handler = new RouteMessageHandler(e -> {
        }, i -> {
        });
        Assertions.assertTrue(handler.handleMessage(new ConnectionRouteMessage(PeerId.createNew(),
                Set.of(PeerId.createNew()), TestUtilities.createEndPoint(1))));
    }

    @Test
    void testHandlesRouteRemovedMessage() {
        final RouteMessageHandler handler = new RouteMessageHandler(e -> {
        }, i -> {
        });
        Assertions.assertTrue(
                handler.handleMessage(new RouteRemovedMessage(PeerId.createNew(), Set.of(PeerId.createNew()))));
    }

    @Test
    void testConnectToNewRoute() {
        final PeerId newPeerId = PeerId.createNew();
        final Set<PeerEndPoint> receivedPeers = new HashSet<>();
        final RouteMessageHandler handler = new RouteMessageHandler(e -> {
            receivedPeers.add(e);
        }, i -> {
        });
        final PeerEndPoint newPeerEndPoint = PeerEndPoint.createFromValues("127.0.0.1", 9000);
        handler.handleMessage(new ConnectionRouteMessage(newPeerId, Set.of(), newPeerEndPoint));
        Assertions.assertEquals(1, receivedPeers.size());
        Assertions.assertEquals(receivedPeers.stream().findFirst().get(), newPeerEndPoint);
    }

    @Test
    void testDisconnectFromRoute() {
        final PeerId remotePeerId = PeerId.createNew();
        final Set<PeerId> receivedPeers = new HashSet<>();
        receivedPeers.add(remotePeerId);
        final RouteMessageHandler handler = new RouteMessageHandler(e -> {
        }, i -> receivedPeers.remove(i));
        handler.handleMessage(new RouteRemovedMessage(remotePeerId, Set.of()));
        Assertions.assertEquals(0, receivedPeers.size());
    }
}
