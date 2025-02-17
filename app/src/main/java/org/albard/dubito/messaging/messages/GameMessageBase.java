package org.albard.dubito.messaging.messages;

import java.util.Set;

import org.albard.dubito.network.PeerId;

public abstract class GameMessageBase implements GameMessage {
    private final PeerId sender;
    private final Set<PeerId> receipients;

    public GameMessageBase(final PeerId sender, final Set<PeerId> receipients) {
        this.sender = sender;
        this.receipients = receipients;
    }

    public PeerId getSender() {
        return this.sender;
    }

    public Set<PeerId> getReceipients() {
        return this.receipients == null ? null : Set.copyOf(this.receipients);
    }
}
