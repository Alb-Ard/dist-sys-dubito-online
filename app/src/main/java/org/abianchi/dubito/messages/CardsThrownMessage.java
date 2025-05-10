package org.abianchi.dubito.messages;

import org.abianchi.dubito.app.gameSession.models.CardType;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CardsThrownMessage extends GameMessageBase {
    private final List<CardType> thrownCards;

    /*
     * uso di JsonCreator per fare in modo che quando il controller invii i
     * messaggi, sappia cosa deve scambiare
     * tra i diversi utenti
     */
    @JsonCreator
    public CardsThrownMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("cards") final List<CardType> thrownCards) {
        super(sender, receipients);
        this.thrownCards = thrownCards;
    }

    public List<CardType> getThrownCards() {
        return thrownCards;
    }
}
