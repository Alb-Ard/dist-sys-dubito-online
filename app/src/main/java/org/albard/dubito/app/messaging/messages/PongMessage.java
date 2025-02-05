package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PongMessage extends GameMessageBase {
    @JsonCreator
    public PongMessage(@JsonProperty("sender") PeerId sender, @JsonProperty("receipients") Set<PeerId> receipients) {
        super(sender, receipients);
    }
}
