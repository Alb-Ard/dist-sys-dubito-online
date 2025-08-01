package org.abianchi.dubito.app.gameSession.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerImpl implements Player {
    private final List<Card> playerHand;

    private int lives;

    public PlayerImpl() {
        this.playerHand = new ArrayList<>();
        this.lives = 2;
    }

    @Override
    public void playCards(final List<Card> cards) {
        for (final Card cardToRemove : cards) {
            this.playerHand.remove(cardToRemove);
        }
    }

    @Override
    public void receiveNewHand(List<Card> newCards) {
        this.playerHand.clear();
        this.playerHand.addAll(newCards);
    }

    @Override
    public void loseLife() {
        this.lives -= 1;
    }

    @Override
    public int getLives() {
        return this.lives;
    }

    @Override
    public void setLives(int lives) { this.lives = lives;}

    @Override
    public List<Card> getHand() {
        return List.copyOf(this.playerHand);
    }

    @Override
    public Optional<String> getName() { return Optional.empty(); }

    @Override
    public String toString() {
        return "PlayerImpl [playerHand=" + playerHand + ", lives=" + lives + "]";
    }

}
