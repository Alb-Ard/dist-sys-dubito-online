package org.abianchi.dubito.app.gameSession.models;


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
}
