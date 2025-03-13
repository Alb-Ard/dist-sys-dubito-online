package org.abianchi.dubito.app.models;

import java.util.Random;

public enum CARDTYPE {
    /**
     * Here are all possible type of cards that can be played
     */
    QUEEN,
    KING,
    ACE,
    JOKER;

    /**
        * Assign a random value type to a card.
        *
        * @return Random value from constants containted in enum for the cards
    */
    public static CARDTYPE getRandomCard() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
