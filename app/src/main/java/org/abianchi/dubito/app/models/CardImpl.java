package org.abianchi.dubito.app.models;

public class CardImpl implements Card{
    private CARDTYPE cardtype;

    public CardImpl() {
        this.cardtype = CARDTYPE.getRandomCard();
    }

    @Override
    public CARDTYPE getCardType() {
        return this.cardtype;
    }
}
