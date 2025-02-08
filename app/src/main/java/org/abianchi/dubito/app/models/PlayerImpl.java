package org.abianchi.dubito.app.models;


import java.util.ArrayList;

public class PlayerImpl implements Player{

    private ArrayList<Card> playerHand;
    private int lives;

    public PlayerImpl() {
        this.playerHand = new ArrayList<>();
        this.lives = 2;
    }

    @Override
    public void playCards(ArrayList<Card> selectedCards) {

    }

    @Override
    public void receiveNewHand(ArrayList<Card> newCards) {
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
    public ArrayList<Card> getHand() {
        return this.playerHand;
    }

}
