package org.abianchi.dubito.app.models;

import java.util.ArrayList;

public interface Player {

    int getLives();

    ArrayList<Card> getHand();
    void playCards(ArrayList<Card> selectedCards);

    void receiveNewHand(ArrayList<Card> newCards);

    void loseRound();

}
