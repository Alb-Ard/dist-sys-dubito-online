package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionController {
    private List<Player> sessionPlayers;

    private GameState gameState;

    //private Player turnPlayer;
    //private Player previousTurnPlayer;
    //private CardType turnCardType;
    private List<Card> turnPrevPlayerPlayedCards;


    public GameSessionController(List<Player> players) {
        this.sessionPlayers = players;
        this.gameState = new GameState();
        //this.newRound();
    }

    public void newRound() {
        this.gameState.newRoundCardType();
        this.giveNewHand();
        this.gameState.nextPlayer(this.getNextAlivePLayingPlayer(this.gameState.getCurrentPlayerIndex()));
        //this.previousTurnPlayer = this.turnPlayer;
        /*this.previousTurnPlayer = this.turnPlayer != null ? // check if we are in the first round
                this.turnPlayer : this.sessionPlayers.get(this.sessionPlayers.size() - 1);
        int currentPlayerIndex = this.sessionPlayers.indexOf(this.previousTurnPlayer);
        this.turnPlayer = getNextAlivePLayingPlayer(currentPlayerIndex);
         */
        /*
        this.turnPlayer = this.sessionPlayers.indexOf(this.previousTurnPlayer) == (this.sessionPlayers.size() - 1) ?
                this.sessionPlayers.get(0) : this.sessionPlayers.get(this.sessionPlayers.indexOf(this.previousTurnPlayer) + 1);

         */
    }

    /**
     * Finds the next alive player in the list, starting from the given index
     * @param startIndex The index to start searching from
     * @return The next player with lives > 0, or null if none found
     */
    private int getNextAlivePLayingPlayer(int startIndex) {
        if (this.sessionPlayers.isEmpty()) {
            return -1;
        }
        int currentIndex = startIndex;
        int playersChecked = 0;

        // Loop through the list until we've checked all players
        while (playersChecked < this.sessionPlayers.size()) {
            currentIndex = (currentIndex + 1) % this.sessionPlayers.size();

            Player nextPlayer = this.sessionPlayers.get(currentIndex);
            if (nextPlayer.getLives() > 0 && nextPlayer.getHand().size() > 0) {
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

    public void playCards(List<Card> playedCards) {
        if(playedCards.size() <= 3) {
            this.turnPrevPlayerPlayedCards = playedCards;
            this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()).playCards(playedCards);
            this.gameState.nextPlayer(this.getNextAlivePLayingPlayer(this.gameState.getCurrentPlayerIndex()));
            /*
            this.turnPlayer.playCards(playedCards);
            this.previousTurnPlayer = this.turnPlayer;
            int currentPlayerIndex = this.sessionPlayers.indexOf(this.previousTurnPlayer);
            this.turnPlayer = this.getNextAlivePLayingPlayer(currentPlayerIndex);
             */
        }
    }

    public void checkLiar() {
        boolean isLiar = false;
        for(Card card : this.turnPrevPlayerPlayedCards) {
            if(card.getCardType().getValue() != this.gameState.getRoundCardValue() && card.getCardType() != CardType.JOKER) {
                isLiar = true;
                break;
            }
        }
        if(isLiar) {
            this.sessionPlayers.get(this.gameState.getPreviousPlayerIndex()).loseLife();
            //this.previousTurnPlayer.loseLife();
            if(!gameOver(this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()))){
                this.newRound();
            } else {
                System.out.println("Game Over");
            }
        } else {
            this.sessionPlayers.get(this.gameState.getCurrentPlayerIndex()).loseLife();
            if(!gameOver(this.sessionPlayers.get(this.gameState.getPreviousPlayerIndex()))){
                this.newRound();
            } else {
                System.out.println("Game Over");
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

    public List<Card> getPlayedCards() {
        return this.turnPrevPlayerPlayedCards;
    }

    public List<Player> getSessionPlayers() {
        return this.sessionPlayers;
    }

    public GameState getCurrentGameState() {
        return this.gameState;
    }

}
