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
}
