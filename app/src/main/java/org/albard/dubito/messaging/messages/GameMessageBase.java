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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + ((receipients == null) ? 0 : receipients.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GameMessageBase other = (GameMessageBase) obj;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        if (receipients == null) {
            if (other.receipients != null)
                return false;
        } else if (!receipients.equals(other.receipients))
            return false;
        return true;
    }
}
