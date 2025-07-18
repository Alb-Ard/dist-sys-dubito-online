package org.albard.dubito.messaging.messages;

import java.util.Set;

import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PingMessage extends GameMessageBase {
    private final PeerEndPoint senderServerEndPoint;

    @JsonCreator
    public PingMessage(@JsonProperty("sender") PeerId sender, @JsonProperty("receipient") Set<PeerId> receipients,
            @JsonProperty("senderServerEndPoint") PeerEndPoint senderServerEndPoint) {
        super(sender, receipients);
        this.senderServerEndPoint = senderServerEndPoint;
    }

    public PeerEndPoint getSenderServerEndPoint() {
        return this.senderServerEndPoint;
    }
}
