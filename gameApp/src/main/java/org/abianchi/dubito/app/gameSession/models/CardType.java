package org.abianchi.dubito.app.gameSession.models;

public enum CardType {
    QUEEN_OF_HEARTS(CardValue.QUEEN), QUEEN_OF_SPADES(CardValue.QUEEN), KING_OF_HEARTS(CardValue.KING),
    KING_OF_SPADES(CardValue.KING), ACE_OF_SPADES(CardValue.ACE), ACE_OF_HEARTS(CardValue.ACE), JOKER(CardValue.JOKER);

    private final CardValue value;

    private CardType(final CardValue value) {
        this.value = value;
    }

    public CardValue getValue() {
        return this.value;
    }
}
