package org.albard.dubito.lobby.messages;

import java.util.List;
import java.util.Set;

import org.albard.dubito.messaging.messages.ErrorGameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LeaveLobbyFailedMessage extends ErrorGameMessageBase {
    @JsonCreator
    public LeaveLobbyFailedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("errors") final List<String> errors) {
        super(sender, receipients, errors);
    }
}
