package org.albard.dubito.app.messaging.handlers;

import java.util.function.BiConsumer;

import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.RouteMessage;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;

public final class RouteMessageHandler implements MessageHandler {
    private final BiConsumer<PeerId, PeerEndPoint> routeReceivedListener;

    public RouteMessageHandler(final BiConsumer<PeerId, PeerEndPoint> routeReceivedListener) {
        this.routeReceivedListener = routeReceivedListener;
    }

    @Override
    public boolean handleMessage(final GameMessage message) {
        if (!(message instanceof RouteMessage routeMessage)) {
            return false;
        }
        if (this.routeReceivedListener != null) {
            this.routeReceivedListener.accept(routeMessage.getSender(), routeMessage.getRouteEndPoint());
        }
        return true;
    }

}
