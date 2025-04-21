package org.abianchi.dubito.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardType;
import org.abianchi.dubito.app.gameSession.models.CardValue;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import java.util.List;
import java.util.Set;

public class RoundCardGeneratedMessage extends GameMessageBase {

    private CardValue roundCard;
    @JsonCreator
    public RoundCardGeneratedMessage(@JsonProperty("sender")final PeerId sender,
                               @JsonProperty("receipients") final Set<PeerId> receipients,
                               @JsonProperty("newRoundCard")final CardValue cardValue) {
        super(sender, receipients);
        this.roundCard = cardValue;
    }

    // questo getter non è proprietà di JSON, quindi aggiungo JsonIgnore
    @JsonIgnore
    public CardValue getRoundCard() {
        return this.roundCard;
    }
}
