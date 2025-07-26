package org.abianchi.dubito.app.gameSession.models;

import java.util.List;
import java.util.Optional;

public interface Player {

    static final int MAX_HAND_SIZE = 5;

    int getLives();

    List<Card> getHand();

    void playCards(List<Card> selectedCards);

    void receiveNewHand(List<Card> newCards);

    void loseLife();

    void setLives(int lives);

    Optional<String> getName();

}
