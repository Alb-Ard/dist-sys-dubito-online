package org.abianchi.dubito.messages;

import java.util.Set;

import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PlayerReadyMessage extends GameMessageBase {
    @JsonCreator
    public PlayerReadyMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients) {
        super(sender, receipients);
    }
}
