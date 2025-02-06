package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class RouteMessage extends GameMessageBase {
    private final PeerEndPoint routeEndPoint;

    @JsonCreator
    public RouteMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("routeEndPoint") final PeerEndPoint routeEndPoint) {
        super(sender, Set.copyOf(receipients));
        this.routeEndPoint = routeEndPoint;
    }

    public PeerEndPoint getRouteEndPoint() {
        return this.routeEndPoint;
    }
}
