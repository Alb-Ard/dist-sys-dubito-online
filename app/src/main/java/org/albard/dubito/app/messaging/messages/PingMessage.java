package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PingMessage extends GameMessageBase {
    @JsonCreator
    public PingMessage(@JsonProperty("sender") PeerId sender, @JsonProperty("receipient") Set<PeerId> receipients) {
        super(sender, receipients);
    }
}
