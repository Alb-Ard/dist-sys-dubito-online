package org.abianchi.dubito.app.gameSession.models;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class CardTypeFactory {
    public static final CardTypeFactory INSTANCE = new CardTypeFactory();
    public static final List<CardType> ALL_CARD_TYPES = Arrays.asList(CardType.values());
    public static final List<CardType> ROUND_VALID_CARD_TYPES = Arrays.stream(CardType.values())
            .filter(x -> x != CardType.JOKER).toList();

    private final Random random = new Random();

    private CardTypeFactory() {
    }

    public CardType createRandomAny() {
        return ALL_CARD_TYPES.get(this.random.nextInt(ALL_CARD_TYPES.size()));
    }

    public CardType createRandomForRound() {
        return ROUND_VALID_CARD_TYPES.get(this.random.nextInt(ROUND_VALID_CARD_TYPES.size()));
    }

    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }
}
