package org.abianchi.dubito.app.gameSession.models;

import java.util.Random;

public class CardTypeFactory {

    public static final CardTypeFactory INSTANCE = new CardTypeFactory();

    private CardTypeFactory() {

    }
    private Random random = new Random();
    public CardType createRandom() {
        return CardType.values()[random.nextInt(CardType.values().length)];
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

}
