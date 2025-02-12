package org.albard.dubito.app.messaging.messages;

import java.util.List;
import java.util.Set;

import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LobbyCreationFailedMessage extends ErrorGameMessageBase {
    @JsonCreator
    public LobbyCreationFailedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("errors") final List<String> errors) {
        super(sender, receipients, errors);
    }

}
