package org.abianchi.dubito.messages;

import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallLiarMessage extends GameMessageBase {

    @JsonCreator
    public CallLiarMessage(@JsonProperty("sender")final PeerId sender,
                           @JsonProperty("receipients")final Set<PeerId> receipients) {
        super(sender, receipients);
    }
}
