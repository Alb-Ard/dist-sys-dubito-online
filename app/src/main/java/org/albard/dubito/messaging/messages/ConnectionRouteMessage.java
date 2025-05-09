package org.albard.dubito.messaging.messages;

import java.util.Set;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ConnectionRouteMessage extends GameMessageBase {
    private final PeerEndPoint routeEndPoint;

    @JsonCreator
    public ConnectionRouteMessage(@JsonProperty("sender") final PeerId sender,
                                  @JsonProperty("receipients") final Set<PeerId> receipients,
                                  @JsonProperty("routeEndPoint") final PeerEndPoint routeEndPoint) {
        super(sender, Set.copyOf(receipients));
        this.routeEndPoint = routeEndPoint;
    }

    public PeerEndPoint getRouteEndPoint() {
        return this.routeEndPoint;
    }
}
