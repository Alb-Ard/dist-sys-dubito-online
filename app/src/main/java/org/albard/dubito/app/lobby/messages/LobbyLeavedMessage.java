package org.albard.dubito.app.lobby.messages;

import java.util.Set;

import org.albard.dubito.app.messaging.messages.GameMessageBase;
import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LobbyLeavedMessage extends GameMessageBase {
    @JsonCreator
    public LobbyLeavedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients) {
        super(sender, receipients);
    }
}
