package org.abianchi.dubito.app.models;


import java.util.List;

public interface Player {

    int getLives();

    List<Card> getHand();
    void playCards(List<Card> selectedCards);

    void receiveNewHand(List<Card> newCards);

    void loseRound();

}
