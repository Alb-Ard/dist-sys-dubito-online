package org.abianchi.dubito.messages;

import org.abianchi.dubito.app.gameSession.models.CardType;
import org.albard.dubito.messaging.messages.GameMessageBase;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.albard.dubito.network.PeerId;

import java.util.List;
import java.util.Set;

public class NewHandDrawnMessage extends GameMessageBase {
    private final List<CardType> newHand;

    @JsonCreator
    public NewHandDrawnMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("newHand") final List<CardType> newHand) {
        super(sender, receipients);
        this.newHand = newHand;
    }

    public List<CardType> getNewHand() {
        return newHand;
    }
}
