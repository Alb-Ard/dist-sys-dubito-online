package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.UserEndPoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PingMessage extends GameMessageBase {
    @JsonCreator
    public PingMessage(@JsonProperty("sender") UserEndPoint sender,
            @JsonProperty("receipients") Set<UserEndPoint> receipients) {
        super(sender, receipients);
    }
}
