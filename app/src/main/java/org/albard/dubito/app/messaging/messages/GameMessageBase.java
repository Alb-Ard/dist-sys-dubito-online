package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.network.PeerId;

public abstract class GameMessageBase implements GameMessage {
    private final PeerId sender;
    private final Set<PeerId> receipients;

    public GameMessageBase(PeerId sender, Set<PeerId> receipients) {
        this.sender = sender;
        this.receipients = receipients == null ? Set.of() : Set.copyOf(receipients);
    }

    public PeerId getSender() {
        return this.sender;
    }

    public Set<PeerId> getReceipients() {
        return Set.copyOf(this.receipients);
    }
}
