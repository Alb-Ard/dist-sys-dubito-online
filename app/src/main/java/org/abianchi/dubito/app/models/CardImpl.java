package org.abianchi.dubito.app.models;


import java.util.Optional;

public class CardImpl implements Card{
    private CARDTYPE cardtype;

    public CardImpl(Optional<CARDTYPE> cardTypeReceived) {

        this.cardtype = cardTypeReceived.isPresent() ? cardTypeReceived.get() : CARDTYPE.getRandomCard();
    }

    @Override
    public CARDTYPE getCardType() {
        return this.cardtype;
    }
}
