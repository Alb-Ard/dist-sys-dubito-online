package org.albard.dubito.messaging.messages;

import java.util.Set;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ConnectionRouteMessage extends GameMessageBase {
    private final PeerId routePeerId;

    private final PeerEndPoint routeEndPoint;

    @JsonCreator
    public ConnectionRouteMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("routePeerId") final PeerId routePeerId,
            @JsonProperty("routeEndPoint") final PeerEndPoint routeEndPoint) {
        super(sender, Set.copyOf(receipients));
        this.routePeerId = routePeerId;
        this.routeEndPoint = routeEndPoint;
    }

    public PeerEndPoint getRouteEndPoint() {
        return this.routeEndPoint;
    }

    public PeerId getRoutePeerId() {
        return this.routePeerId;
    }
}
