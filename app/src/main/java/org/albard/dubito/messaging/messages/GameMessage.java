package org.albard.dubito.messaging.messages;

import java.util.Set;

import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface GameMessage {
    PeerId getSender();

    Set<PeerId> getReceipients();

    @JsonIgnore
    default boolean isBroadcast() {
        return this.getReceipients() == null;
    }
}
