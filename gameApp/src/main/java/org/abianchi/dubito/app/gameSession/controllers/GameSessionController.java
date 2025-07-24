package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.*;
import org.albard.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionController<X extends Player> {
    private final List<X> sessionPlayers;
    private final GameState gameState;

    public GameSessionController(List<X> players) {
        this.sessionPlayers = players;
        this.gameState = new GameState();
    }

    public void newRound() {
        if (canGenerateRoundCard()) {
            this.getCurrentGameState().setRandomRoundCardType();
        }
        this.giveNewHand();
        this.advanceTurn(List.of());
    }

    /**
     * simple check method to see if player with given index is the current active player
     * @param index index of the player
     * @return boolean that returns if the player is the current active one
     */
    public boolean isActivePlayer(final int index) {
        return this.getCurrentGameState().getCurrentPlayerIndex().map(x -> x == index).orElse(false);
    }

    /**
     * Simple method to determine whether this controller can generate new CardValues for the new rounds (in the offline
     * version it's always true, but this method is overridden for the online version)
     *
     * @return a boolean to determine whether or not this version of the controller can generate new CardValues
     */
    protected boolean canGenerateRoundCard() {
        return true;
    }

    /**
     * Finds the next alive player in the list, starting from the given index
     *
     * @return Optional containing the next player with lives > 0, or optional empty if non were found
     */
    private Optional<Integer> getNextAlivePlayer() {
        if (this.getCurrentGameState().getCurrentPlayerIndex().isEmpty()) {
            return Optional.of(0);
        }
        return this.getCurrentGameState().getCurrentPlayerIndex().flatMap(startIndex -> {
            // Loop through the list until we've checked all players
            for (int playersChecked = 1; playersChecked < this.sessionPlayers.size(); playersChecked++) {
                final int currentIndex = (startIndex + playersChecked) % this.sessionPlayers.size();
                final Player nextPlayer = this.sessionPlayers.get(currentIndex);
                if (nextPlayer.getLives() > 0 && !nextPlayer.getHand().isEmpty()) {
                    return Optional.of(currentIndex);
                }
            }

            // No alive players found
            return Optional.empty();
        });
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

    public void playCards(final List<Card> cards) {
        this.getCurrentPlayer().ifPresent(currentPlayer -> {
            // Let the player play only cards that it has in its hand
            if (!currentPlayer.getHand().containsAll(cards)) {
                Logger.logInfo("Player Cards not found in current hand");
                return;
            }
            if (cards.size() <= 3) {
                Logger.logInfo("Playing " + cards.size() + " cards");
                currentPlayer.playCards(cards);
                this.advanceTurn(cards);
            }
        });
    }

    // Updates the game state to advance to the next player, if there is one, or
    // ends the game if a winner is found
    private void advanceTurn(final List<Card> previousPlayerCards) {
        // Set the previous player cards
        this.getCurrentGameState().setPreviousPlayerPlayedCards(previousPlayerCards);
        // If the next player is found, set it
        this.getNextAlivePlayer().ifPresentOrElse(this.getCurrentGameState()::nextPlayer, () -> {
            // Otherwise, search for a winner and set it
            this.findWinner().map(this.getSessionPlayers()::indexOf)
                    .ifPresentOrElse(this.getCurrentGameState()::setWinnerPlayerIndex, this::newRound);
        });
    }

    public void callLiar() {
        final List<Card> prevPlayedCards = this.getCurrentGameState().getPreviousPlayerPlayedCards();
        if (prevPlayedCards.isEmpty()) {
            return;
        }
        this.getCurrentGameState().getRoundCardValue().ifPresent(roundCard -> {
            this.getCurrentGameState().getPreviousPlayerIndex().ifPresent(previousPlayerIndex -> {
                this.getCurrentGameState().getCurrentPlayerIndex().ifPresent(currentPlayerIndex -> {
                    boolean isLiar = false;
                    for (final Card card : prevPlayedCards) {
                        if (card.getCardType().getValue() != roundCard && card.getCardType() != CardType.JOKER) {
                            isLiar = true;
                            break;
                        }
                    }
                    if (isLiar) {
                        this.sessionPlayers.get(previousPlayerIndex).loseLife();
                        if (findWinner().isEmpty()) {
                            this.newRound();
                        } else {
                            this.getCurrentGameState().setWinnerPlayerIndex(currentPlayerIndex);
                        }
                    } else {
                        this.sessionPlayers.get(currentPlayerIndex).loseLife();
                        if (findWinner().isEmpty()) {
                            this.newRound();
                        } else {
                            this.getCurrentGameState().setWinnerPlayerIndex(previousPlayerIndex);
                        }
                    }
                });

            });
        });
    }

    public Optional<X> findWinner() {
        final List<X> alivePlayers = this.sessionPlayers.stream().filter(el -> el.getLives() > 0).toList();
        return alivePlayers.size() == 1 ? Optional.of(alivePlayers.getFirst()) : Optional.empty();
    }

    public void removePlayer(final int removedPlayerIndex) {
        this.sessionPlayers.get(removedPlayerIndex).setLives(0);
        // verifichiamo prima se c'è un vincitore
        final Optional<X> possibleWinner = findWinner();
        if (possibleWinner.isPresent()) {
            Logger.logInfo("We have a winner");
            this.getCurrentGameState().setWinnerPlayerIndex(this.getSessionPlayers().indexOf(possibleWinner.get()));
        }
        // copre il caso in cui va via il giocatore non attivo e la partita deve
        // continuare
        else if (this.getCurrentGameState().getCurrentPlayerIndex().map(x -> x != removedPlayerIndex).orElse(true)) {
            Logger.logInfo("the game continues");
            return;
        } else {
            // se non è il giocatore attivo e dobbiamo iniziare il secondo round
            Logger.logInfo("Moving into next round");
            this.advanceTurn(this.getCurrentGameState().getPreviousPlayerPlayedCards());
        }
    }

    public List<X> getSessionPlayers() {
        return List.copyOf(this.sessionPlayers);
    }

    public GameState getCurrentGameState() {
        return this.gameState;
    }

    public Optional<X> getPreviousPlayer() {
        return this.getCurrentGameState().getPreviousPlayerIndex().map(sessionPlayers::get);
    }

    public Optional<X> getCurrentPlayer() {
        return this.getCurrentGameState().getCurrentPlayerIndex().map(sessionPlayers::get);
    }

    public Optional<X> getWinnerPlayer() {
        return this.getCurrentGameState().getWinnerPlayerIndex().map(sessionPlayers::get);
    }
}
