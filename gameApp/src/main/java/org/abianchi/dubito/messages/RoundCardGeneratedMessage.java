package org.abianchi.dubito.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.abianchi.dubito.app.gameSession.models.CardValue;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import java.util.Set;

public class RoundCardGeneratedMessage extends GameMessageBase {
    private final CardValue roundCard;

    @JsonCreator
    public RoundCardGeneratedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("roundCard") final CardValue roundCard) {
        super(sender, receipients);
        this.roundCard = roundCard;
    }

    public CardValue getRoundCard() {
        return this.roundCard;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((roundCard == null) ? 0 : roundCard.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoundCardGeneratedMessage other = (RoundCardGeneratedMessage) obj;
        if (roundCard != other.roundCard)
            return false;
        return true;
    }
}
