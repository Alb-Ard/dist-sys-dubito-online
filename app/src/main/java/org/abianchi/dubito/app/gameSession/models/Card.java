package org.abianchi.dubito.app.gameSession.models;

import java.util.Optional;

/**
 * Card Model for every possible card that can be played during the game
 */

public interface Card {
    CardType getCardType();

    public static Card random() {
        return new CardImpl(Optional.empty());
    }

    public static Card ofType(final CardType type) {
        return new CardImpl(Optional.of(type));
    }
}
