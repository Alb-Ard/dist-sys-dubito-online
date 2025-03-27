package org.abianchi.dubito.app.gameSession.models;

public class GameState {

    private int currentPlayerIndex;
    private int previousPlayerIndex;
    private CardValue roundCardValue;

    public GameState() {
        this.currentPlayerIndex = 0;
        this.previousPlayerIndex = -1;
        this.roundCardValue = CardType.getRandomCard().getValue();
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

    public int getCurrentPlayerIndex() {
        return this.currentPlayerIndex;
    }

    public int getPreviousPlayerIndex() {
        return this.previousPlayerIndex;
    }

    public CardValue getRoundCardValue() {
        return this.roundCardValue;
    }
}
