package org.albard.dubito.userManagement.messages;

import java.util.Set;

import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateUserMessage extends GameMessageBase {
    private final String userName;

    @JsonCreator
    public UpdateUserMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients, @JsonProperty("name") final String userName) {
        super(sender, receipients);
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }
}
