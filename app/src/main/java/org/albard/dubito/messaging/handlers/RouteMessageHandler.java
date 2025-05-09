package org.albard.dubito.messaging.handlers;

import java.util.function.Consumer;

import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.ConnectionRouteMessage;
import org.albard.dubito.messaging.messages.RouteRemovedMessage;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;

public final class RouteMessageHandler implements MessageHandler {
    private final Consumer<PeerEndPoint> routeAddedListener;
    private final Consumer<PeerId> routeRemovedListener;

    public RouteMessageHandler(final Consumer<PeerEndPoint> routeAddedListener,
            final Consumer<PeerId> routeRemovedListener) {
        this.routeAddedListener = routeAddedListener;
        this.routeRemovedListener = routeRemovedListener;
    }

    @Override
    public boolean handleMessage(final GameMessage message) {
        if (message instanceof ConnectionRouteMessage routeMessage) {
            if (this.routeAddedListener != null) {
                this.routeAddedListener.accept(routeMessage.getRouteEndPoint());
            }
            return true;
        }
        if (message instanceof RouteRemovedMessage routeMessage) {
            if (this.routeRemovedListener != null) {
                this.routeRemovedListener.accept(routeMessage.getSender());
            }
            return true;
        }
        return false;
    }

}
