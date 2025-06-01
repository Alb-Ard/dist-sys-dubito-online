package org.abianchi.dubito.app.gameSession.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameState {
    private Optional<CardValue> roundCardValue;
    private List<Card> previousPlayerPlayedCards;
    private Optional<Integer> currentPlayerIndex;
    private Optional<Integer> previousPlayerIndex;
    private Optional<Integer> winnerPlayerIndex;

    public GameState() {
        this.currentPlayerIndex = Optional.empty();
        this.previousPlayerIndex = Optional.empty();
        this.winnerPlayerIndex = Optional.empty();
        this.previousPlayerPlayedCards = new ArrayList<>();
        this.roundCardValue = Optional.empty();
    }

    public void nextPlayer(final int nextPlayerIndex) {
        this.previousPlayerIndex = this.currentPlayerIndex;
        this.currentPlayerIndex = Optional.of(nextPlayerIndex);
    }

    public void setRandomRoundCardType() {
        this.roundCardValue = Optional.of(CardTypeFactory.INSTANCE.createRandomForRound().getValue());
    }

    public void setRoundCardType(CardValue value) {
        this.roundCardValue = Optional.of(value);
    }

    public void setPreviousPlayerPlayedCards(List<Card> playedCards) {
        this.previousPlayerPlayedCards.clear();
        this.previousPlayerPlayedCards.addAll(playedCards);
    }

    public void setWinnerPlayerIndex(final int winnerIndex) {
        this.winnerPlayerIndex = Optional.of(winnerIndex);
    }

    public Optional<Integer> getCurrentPlayerIndex() {
        return this.currentPlayerIndex;
    }

    public Optional<Integer> getPreviousPlayerIndex() {
        return this.previousPlayerIndex;
    }

    public List<Card> getPreviousPlayerPlayedCards() {
        return List.copyOf(this.previousPlayerPlayedCards);
    }

    public Optional<CardValue> getRoundCardValue() {
        return this.roundCardValue;
    }

    public Optional<Integer> getWinnerPlayerIndex() {
        return this.winnerPlayerIndex;
    }

    @Override
    public String toString() {
        return "GameState [roundCardValue=" + roundCardValue + ", previousPlayerPlayedCards="
                + previousPlayerPlayedCards + ", currentPlayerIndex=" + currentPlayerIndex + ", previousPlayerIndex="
                + previousPlayerIndex + ", winnerPlayerIndex=" + winnerPlayerIndex + "]";
    }
}
