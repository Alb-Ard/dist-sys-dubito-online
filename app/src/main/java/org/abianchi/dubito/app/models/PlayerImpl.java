package org.abianchi.dubito.app.models;


import java.util.ArrayList;
import java.util.List;

public class PlayerImpl implements Player{

    private List<Card> playerHand;
    private int lives;

    public PlayerImpl() {
        this.playerHand = new ArrayList<>();
        this.lives = 2;
    }

    @Override
    public void playCards(List<Card> selectedCards) {
        this.playerHand.removeAll(selectedCards);
    }

    @Override
    public void receiveNewHand(List<Card> newCards) {
        this.playerHand.clear();
        this.playerHand.addAll(newCards);
    }

    @Override
    public void loseRound() {
        this.lives -= 1;
    }

    @Override
    public int getLives() {
        return this.lives;
    }

    @Override
    public List<Card> getHand() {
        return this.playerHand;
    }

}
