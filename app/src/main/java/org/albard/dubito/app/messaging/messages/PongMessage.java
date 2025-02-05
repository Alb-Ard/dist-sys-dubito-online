package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.UserEndPoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PongMessage extends GameMessageBase {
    @JsonCreator
    public PongMessage(@JsonProperty("sender") UserEndPoint sender,
            @JsonProperty("receipients") Set<UserEndPoint> receipients) {
        super(sender, receipients);
    }
}
