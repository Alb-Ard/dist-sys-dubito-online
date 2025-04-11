package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionController {
    private List<Player> sessionPlayers;

    private GameState gameState;

    private List<Card> selectedCards;

    private int winnerIndex;



    public GameSessionController(List<Player> players) {
        this.sessionPlayers = players;
        this.gameState = new GameState();
        this.selectedCards = new ArrayList<>();
    }

    public void newRound() {
        this.gameState.newRoundCardType();
        this.giveNewHand();
        this.gameState.setTurnPrevPlayerPlayedCards(List.of());
        int nextPlayerIndex = this.getNextAlivePLayingPlayer(this.gameState.getCurrentPlayerIndex());
        if(nextPlayerIndex == -1 && !gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))) {
            this.newRound();
        } else {
            this.gameState.nextPlayer(nextPlayerIndex);
        }

    }

    /**
     * Finds the next alive player in the list, starting from the given index
     * @param startIndex The index to start searching from
     * @return The next player with lives > 0, or null if none found
     */
    private int getNextAlivePLayingPlayer(int startIndex) {
        // this is the first round
        if (startIndex < 0) {
            return 0;
        }
        int currentIndex = startIndex;
        int playersChecked = 1;

        // Loop through the list until we've checked all players
        while (playersChecked < this.sessionPlayers.size()) {
            currentIndex = (currentIndex + 1) % this.sessionPlayers.size();

            Player nextPlayer = this.sessionPlayers.get(currentIndex);
            if (nextPlayer.getLives() > 0 && !nextPlayer.getHand().isEmpty()) {
                return currentIndex;
            }
            playersChecked++;
        }

        // No alive players found
        return -1;
    }

    private void giveNewHand() {
        this.sessionPlayers.forEach(player -> {
            if (player.getLives() > 0) {
                List<Card> newHand = new ArrayList<>();
                for (int i = 0; i < Player.MAXHANDSIZE; i++) {
                    newHand.add(new CardImpl(Optional.empty()));
                }
                player.receiveNewHand(newHand);
            }
        });
    }

    public void selectCard(Card selectedCard) {
        this.selectedCards.add(selectedCard);
    }

    public void removeSelectedCard(Card card) {
        this.selectedCards.remove(card);
    }

    public void playCards() {
        if(this.selectedCards.size() <= 3) {
            this.gameState.setTurnPrevPlayerPlayedCards(this.selectedCards);
            this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()).playCards(this.selectedCards);
            this.selectedCards.clear();
            int nextPlayerIndex = this.getNextAlivePLayingPlayer(this.gameState.getCurrentPlayerIndex());
            if(nextPlayerIndex == -1 && !gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))) {
                this.newRound();
            } else {
                this.gameState.nextPlayer(nextPlayerIndex);
            }
        }
    }

    public void callLiar() {
        List<Card> prevPlayedCards = this.gameState.getTurnPrevPlayerPlayedCards();
        if(prevPlayedCards.isEmpty()) {
            return;
        }
        boolean isLiar = false;
        for(Card card : prevPlayedCards) {
            if(card.getCardType().getValue() != this.gameState.getRoundCardValue() && card.getCardType() != CardType.JOKER) {
                isLiar = true;
                break;
            }
        }
        if(isLiar) {
            this.sessionPlayers.get(this.gameState.getPreviousPlayerIndex()).loseLife();
            if(!gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))){
                this.newRound();
            } else {
                this.winnerIndex = this.gameState.getCurrentPlayerIndex();
            }
        } else {
            this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()).loseLife();
            if(!gameOver(this.sessionPlayers.get(this.gameState.getPreviousPlayerIndex()))){
                this.newRound();
            } else {
                this.winnerIndex = this.gameState.getPreviousPlayerIndex();
            }
        }
    }

    public boolean gameOver(Player possibleWinner) {
        boolean gameOver = true;
        for(Player sessionPlayer : this.sessionPlayers) {
            if(sessionPlayer.getLives() != 0 && !sessionPlayer.equals(possibleWinner)) {
                gameOver = false;
                break;
            }
        }
        return gameOver;
    }

    public List<Player> getSessionPlayers() {
        return List.copyOf(this.sessionPlayers);
    }

    public GameState getCurrentGameState() {
        return this.gameState;
    }

    public List<Card> getSelectedCards() {
        return this.selectedCards;
    }

    public Player getPreviousPlayer() { return sessionPlayers.get(this.gameState.getPreviousPlayerIndex());}

    public Player getCurrentPlayer() { return sessionPlayers.get(this.gameState.getCurrentPlayerIndex());}

    public int getWinnerIndex() { return this.winnerIndex;}

}
