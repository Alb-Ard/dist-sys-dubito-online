package org.abianchi.dubito.app.gameSession.models;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private int currentPlayerIndex;
    private int previousPlayerIndex;
    private CardValue roundCardValue;
    private List<Card> turnPrevPlayerPlayedCards;

    public GameState() {
        this.currentPlayerIndex = -1;
        this.previousPlayerIndex = -2;
        this.roundCardValue = CardType.getRandomCard().getValue();
        this.turnPrevPlayerPlayedCards = new ArrayList<>();
    }

    public void nextPlayer(int nextPlayerIndex) {
        this.previousPlayerIndex = this.currentPlayerIndex;
        this.currentPlayerIndex = nextPlayerIndex;
    }

    public void newRoundCardType() {
        do {
            this.roundCardValue = CardType.getRandomCard().getValue();
        } while (this.roundCardValue == CardValue.JOKER);
    }

    public void setTurnPrevPlayerPlayedCards(List<Card> playedCards) {
        this.turnPrevPlayerPlayedCards.clear();
        this.turnPrevPlayerPlayedCards.addAll(playedCards);
    }

    public int getCurrentPlayerIndex() {
        return this.currentPlayerIndex;
    }

    public int getPreviousPlayerIndex() {
        return this.previousPlayerIndex;
    }

    public List<Card> getTurnPrevPlayerPlayedCards() {return  this.turnPrevPlayerPlayedCards;}

    public CardValue getRoundCardValue() {
        return this.roundCardValue;
    }
}
