package org.abianchi.dubito.app.gameSession.models;


import java.util.Objects;
import java.util.Optional;

public class CardImpl implements Card{
    private CardType cardtype;

    public CardImpl(Optional<CardType> cardTypeReceived) {

        this.cardtype = cardTypeReceived.isPresent() ? cardTypeReceived.get() : CardTypeFactory.INSTANCE.createRandom();
    }

    @Override
    public CardType getCardType() {
        return this.cardtype;
    }

    // creati equals e hashcode per cambiare come vengano rimosse dalla mano le carte dei giocatori (non abbimao poi problemi online)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardImpl card = (CardImpl) o;
        return cardtype == card.cardtype;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardtype);
    }
}
