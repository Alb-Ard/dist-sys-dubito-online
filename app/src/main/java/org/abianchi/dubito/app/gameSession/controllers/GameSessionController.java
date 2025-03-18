package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.CARDTYPE;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardImpl;
import org.abianchi.dubito.app.gameSession.models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionController {

    private static final int MAXHANDSIZE = 5;
    private List<Player> sessionPlayers;
    private Player turnPlayer;
    private Player previousTurnPlayer;
    private List<Card> turnPrevPlayerPlayedCards;
    private CARDTYPE turnCardType;

    public GameSessionController(List<Player> players) {
        this.sessionPlayers = players;
        this.newRound();
    }

    public void newRound() {
        this.chooseRandomRoundCard();
        this.giveNewHand();
        //this.previousTurnPlayer = this.turnPlayer;
        this.previousTurnPlayer = this.turnPlayer != null ? // check if we are in the first round
                this.turnPlayer : this.sessionPlayers.get(this.sessionPlayers.size() - 1);
        int currentPlayerIndex = this.sessionPlayers.indexOf(this.previousTurnPlayer);
        this.turnPlayer = getNextAlivePLayingPlayer(currentPlayerIndex);
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
    private Player getNextAlivePLayingPlayer(int startIndex) {
        if (this.sessionPlayers.isEmpty()) {
            return null;
        }
        int currentIndex = startIndex;
        int playersChecked = 0;

        // Loop through the list until we've checked all players
        while (playersChecked < this.sessionPlayers.size()) {
            currentIndex = (currentIndex + 1) % this.sessionPlayers.size();

            Player nextPlayer = this.sessionPlayers.get(currentIndex);
            if (nextPlayer.getLives() > 0 && nextPlayer.getHand().size() > 0) {
                return nextPlayer;
            }
            playersChecked++;
        }

        // No alive players found
        return null;
    }

    private void giveNewHand() {
        this.sessionPlayers.forEach(player -> {
            if (player.getLives() > 0) {
                List<Card> newHand = new ArrayList<>();
                for (int i = 0; i < MAXHANDSIZE; i++) {
                    newHand.add(new CardImpl(Optional.empty()));
                }
                player.receiveNewHand(newHand);
            }
        });
    }

    private void chooseRandomRoundCard() {
        do {
            this.turnCardType = CARDTYPE.getRandomCard();
        } while (this.turnCardType == CARDTYPE.JOKER);
    }

    public void playCards(List<Card> playedCards) {
        if(playedCards.size() <= 3) {
            this.turnPrevPlayerPlayedCards = playedCards;
            this.turnPlayer.playCards(playedCards);
            this.previousTurnPlayer = this.turnPlayer;
            int currentPlayerIndex = this.sessionPlayers.indexOf(this.previousTurnPlayer);
            this.turnPlayer = this.getNextAlivePLayingPlayer(currentPlayerIndex);
        }
    }

    public void checkLiar() {
        boolean isLiar = false;
        for(Card card : this.turnPrevPlayerPlayedCards) {
            if(card.getCardType() != this.turnCardType && card.getCardType() != CARDTYPE.JOKER) {
                isLiar = true;
                break;
            }
        }
        if(isLiar) {
            this.previousTurnPlayer.loseRound();
            this.newRound();
        } else {
            this.turnPlayer.loseRound();
            this.newRound();
        }
    }

    public Player getCurrentTurnPlayer() {
        return this.turnPlayer;
    }

    public Player getPreviousTurnPlayer() {
        return this.previousTurnPlayer;
    }

    public List<Card> getPlayedCards() {
        return this.turnPrevPlayerPlayedCards;
    }

    public List<Player> getSessionPlayers() {
        return this.sessionPlayers;
    }

    public CARDTYPE getTurnCardType() {
        return this.turnCardType;
    }

}
