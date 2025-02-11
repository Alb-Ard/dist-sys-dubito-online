package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class RouteRemovedMessage extends GameMessageBase {
    @JsonCreator
    public RouteRemovedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients) {
        super(sender, Set.copyOf(receipients));
    }
}
