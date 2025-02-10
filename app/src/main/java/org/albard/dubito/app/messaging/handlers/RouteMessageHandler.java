package org.albard.dubito.app.messaging.handlers;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.RouteAddedMessage;
import org.albard.dubito.app.messaging.messages.RouteRemovedMessage;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;

public final class RouteMessageHandler implements MessageHandler {
    private final BiConsumer<PeerId, PeerEndPoint> routeAddedListener;
    private final Consumer<PeerId> routeRemovedListener;

    public RouteMessageHandler(final BiConsumer<PeerId, PeerEndPoint> routeAddedListener,
            final Consumer<PeerId> routeRemovedListener) {
        this.routeAddedListener = routeAddedListener;
        this.routeRemovedListener = routeRemovedListener;
    }

    @Override
    public boolean handleMessage(final GameMessage message) {
        if (message instanceof RouteAddedMessage routeMessage) {
            if (this.routeAddedListener != null) {
                this.routeAddedListener.accept(routeMessage.getSender(), routeMessage.getRouteEndPoint());
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
