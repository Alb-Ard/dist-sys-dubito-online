package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class GameSession3PlayersTest {
    private static final int MAXPLAYERS = 3;

    private final List<Player> testPlayers = new ArrayList<>();
    private GameSessionController<Player> testController;

    @BeforeEach
    void setUp() {
        for (int i = 0; i < MAXPLAYERS; i++) {
            this.testPlayers.add(new PlayerImpl());
        }
        CardTypeFactory.INSTANCE.setSeed(14000);
        this.testController = new GameSessionController<>(this.testPlayers);
        this.testController.newRound();
    }

    @Test
    void assertStart() {
        List<Player> sessionPlayers = this.testController.getSessionPlayers();
        GameState gameState = this.testController.getCurrentGameState();
        Assertions.assertEquals(sessionPlayers.size(), MAXPLAYERS);
        Assertions.assertEquals(this.testPlayers.get(0), sessionPlayers.get(gameState.getCurrentPlayerIndex().get()));
        Assertions.assertTrue(gameState.getPreviousPlayerIndex().isEmpty());
        sessionPlayers.forEach(player -> {
            Assertions.assertEquals(player.getHand().size(), Player.MAX_HAND_SIZE);
            player.getHand().forEach(Assertions::assertNotNull);
        });
    }

    @Test
    void assertPlayedAllCardsNewRound() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundOneValue = currentGameState.getRoundCardValue().get();

        //i make player 1 play 3 cards
        Player player1 = this.testController.getCurrentPlayer().get();
        List<Card> player1Hand = player1.getHand();
        List<Card> player1FirstPlayedCards = player1Hand.subList(0, 3).stream().toList();
        this.testController.playCards(player1FirstPlayedCards);

        // i make player 2 play 2 cards
        Player player2 = this.testController.getCurrentPlayer().get();
        System.out.println(player2);
        List<Card> player2Hand = player2.getHand();
        List<Card> player2FirstPlayedCards = player2Hand.subList(0, 2).stream().toList();
        this.testController.playCards(player2FirstPlayedCards);

        // i also make player 3 play 3 cards
        Player player3 = this.testController.getCurrentPlayer().get();
        List<Card> player3Hand = player3.getHand();
        List<Card> player3FirstPlayedCards = player3Hand.subList(0, 3).stream().toList();
        this.testController.playCards(player1FirstPlayedCards);

        // i make every players play all their cards
        this.testController.playCards(this.testController.getCurrentPlayer().get().getHand()); //player 1
        this.testController.playCards(this.testController.getCurrentPlayer().get().getHand()); //player 2
        this.testController.playCards(this.testController.getCurrentPlayer().get().getHand()); //player 3

        // i get the new hand for the second round
        List<Card> player1HandRound2 = this.testController.getCurrentPlayer().get().getHand();
        List<Card> player2HandRound2 = this.testController.getSessionPlayers().get(1).getHand();
        List<Card> player3HandRound2 = this.testController.getPreviousPlayer().get().getHand();

        // checking here that both players did not lose a life, that there is a new round card value and that they both
        // have a different hand now
        for(int i = 0 ; i < MAXPLAYERS ; i++) {
            Assertions.assertEquals(2, this.testController.getSessionPlayers().get(i).getLives());
        }
        Assertions.assertNotEquals(player1HandRound2, player1Hand);
        Assertions.assertNotEquals(player2HandRound2, player2Hand);
        Assertions.assertNotEquals(player3HandRound2, player3Hand);
        Assertions.assertNotEquals(roundOneValue, currentGameState.getRoundCardValue());
    }

    @Test
    void assertPlayerLeavesGameContinues() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue currentCardValue = currentGameState.getRoundCardValue().get();
        Player player1 = this.testController.getCurrentPlayer().get();
        Player player3 = this.testController.getSessionPlayers().get(2);
        List<Card> player1Hand = player1.getHand();
        List<Card> player1FirstPlayedCards = player1Hand.subList(0, 3).stream().toList();
        this.testController.playCards(player1FirstPlayedCards);
        this.testController.removePlayer(currentGameState.getCurrentPlayerIndex().get());
        Assertions.assertEquals(player3, this.testController.getCurrentPlayer().get());
        Assertions.assertEquals(0, this.testController.getPreviousPlayer().get().getLives());
        Assertions.assertTrue(currentGameState.getWinnerPlayerIndex().isEmpty());
        Assertions.assertEquals(currentCardValue, currentGameState.getRoundCardValue().get());
    }

    @Test
    void assertGameOver() {
        GameState currentGameState = this.testController.getCurrentGameState();
        // did a loop here to make at least 2 players lose all their lives
        for (int i = 0; i < 5 ; i++) {
            CardValue roundCardValue = currentGameState.getRoundCardValue().get();
            Player liarPlayer = this.testController.getCurrentPlayer().get();
            List<Card> currentHand = liarPlayer.getHand();
            List<Card> liarCards = this.getLiarCards(currentHand, roundCardValue);
            this.testController.playCards(liarCards);
            this.testController.callLiar();
        }


        // check here if player 1 and player 2 lost all lives
        Assertions.assertEquals(0, this.testController.getSessionPlayers().get(0).getLives());
        Assertions.assertEquals(0, this.testController.getSessionPlayers().get(2).getLives());
        // check if new round has moved the turn
        Assertions.assertTrue(currentGameState.getWinnerPlayerIndex().isPresent());
        Assertions.assertEquals(1, currentGameState.getWinnerPlayerIndex().get());
    }

    private List<Card> getLiarCards(List<Card> playerHand, CardValue roundCardValue) {
        List<Card> testLiarCards = new ArrayList<>();
        for (Card card : playerHand) {
            if (card.getCardType().getValue() != roundCardValue && card.getCardType() != CardType.JOKER) {
                testLiarCards.add(card);
            }
        }
        if (testLiarCards.size() > 3) {
            testLiarCards = new ArrayList<>(testLiarCards.subList(0, 2));
        }
        return testLiarCards;
    }
}
