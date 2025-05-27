package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionController<X extends Player> {
    private final List<X> sessionPlayers;

    private final GameState gameState;

    private final List<Card> selectedCards;

    private int winnerIndex;

    public GameSessionController(List<X> players) {
        this.sessionPlayers = players;
        this.gameState = new GameState();
        this.selectedCards = new ArrayList<>();
    }

    public void newRound() {
        if (canGenerateRoundCard()) {
            this.gameState.newRoundCardType();
        }
        this.giveNewHand();
        this.gameState.setTurnPrevPlayerPlayedCards(List.of());
        int nextPlayerIndex = this.getNextAlivePlayingPlayer(this.gameState.getCurrentPlayerIndex());
        if (nextPlayerIndex == -1 && !gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))) {
            this.newRound();
        } else {
            this.gameState.nextPlayer(nextPlayerIndex);
        }
    }

    public boolean isActivePlayer(final int index) {
        return index == this.gameState.getCurrentPlayerIndex();
    }

    protected boolean canGenerateRoundCard() {
        return true;
    }

    /**
     * Finds the next alive player in the list, starting from the given index
     * 
     * @param startIndex The index to start searching from
     * @return The next player with lives > 0, or null if none found
     */
    private int getNextAlivePlayingPlayer(int startIndex) {
        // this is the first round
        if (startIndex < 0) {
            return 0;
        }

        // Loop through the list until we've checked all players
        for (int playersChecked = 1 ; playersChecked < this.sessionPlayers.size() ; playersChecked ++) {
            final int currentIndex = (startIndex + playersChecked) % this.sessionPlayers.size();

            final Player nextPlayer = this.sessionPlayers.get(currentIndex);
            if (nextPlayer.getLives() > 0 && !nextPlayer.getHand().isEmpty()) {
                return currentIndex;
            }
        }

        // No alive players found
        return -1;
    }

    protected void giveNewHand() {
        this.sessionPlayers.forEach(player -> {
            if (player.getLives() > 0) {
                List<Card> newHand = new ArrayList<>();
                for (int i = 0; i < Player.MAX_HAND_SIZE; i++) {
                    newHand.add(Card.random());
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
        if (this.selectedCards.size() <= 3) {
            this.gameState.setTurnPrevPlayerPlayedCards(this.selectedCards);
            this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()).playCards(this.selectedCards);
            this.selectedCards.clear();
            int nextPlayerIndex = this.getNextAlivePlayingPlayer(this.gameState.getCurrentPlayerIndex());
            if (nextPlayerIndex == -1 && !gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))) {
                this.newRound();
            } else {
                this.gameState.nextPlayer(nextPlayerIndex);
            }
        }
    }

    public void callLiar() {
        List<Card> prevPlayedCards = this.gameState.getTurnPrevPlayerPlayedCards();
        if (prevPlayedCards.isEmpty()) {
            return;
        }
        boolean isLiar = false;
        for (Card card : prevPlayedCards) {
            if (card.getCardType().getValue() != this.gameState.getRoundCardValue()
                    && card.getCardType() != CardType.JOKER) {
                isLiar = true;
                break;
            }
        }
        if (isLiar) {
            this.sessionPlayers.get(this.gameState.getPreviousPlayerIndex()).loseLife();
            if (!gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))) {
                this.newRound();
            } else {
                this.winnerIndex = this.gameState.getCurrentPlayerIndex();
            }
        } else {
            this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()).loseLife();
            if (!gameOver(this.sessionPlayers.get(this.gameState.getPreviousPlayerIndex()))) {
                this.newRound();
            } else {
                this.winnerIndex = this.gameState.getPreviousPlayerIndex();
            }
        }
    }

    public boolean gameOver(Player possibleWinner) {
        boolean gameOver = true;
        for (Player sessionPlayer : this.sessionPlayers) {
            if (sessionPlayer.getLives() != 0 && !sessionPlayer.equals(possibleWinner)) {
                gameOver = false;
                break;
            }
        }
        return gameOver;
    }

    public void removePlayer(int removedPlayer) {
        this.sessionPlayers.get(removedPlayer).setLives(0);
        // copre il caso in cui va via il giocatore non attivo e la partita deve continuare
        if(removedPlayer != this.getCurrentGameState().getCurrentPlayerIndex() && this.sessionPlayers.isEmpty()) {
            return;
        }
        if (!gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))) {
            this.newRound();
        } else {
            this.winnerIndex = this.gameState.getCurrentPlayerIndex();
        }
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

    public Optional<Player> getPreviousPlayer() {
        final int index = this.gameState.getPreviousPlayerIndex();
        return index < 0 ? Optional.empty() : Optional.of(sessionPlayers.get(index));
    }

    public Optional<Player> getCurrentPlayer() {
        final int index = this.gameState.getCurrentPlayerIndex();
        return index < 0 ? Optional.empty() : Optional.of(sessionPlayers.get(index));
    }

    public int getWinnerIndex() {
        return this.winnerIndex;
    }

    protected List<X> getPlayers() {
        return this.sessionPlayers;
    }

}
