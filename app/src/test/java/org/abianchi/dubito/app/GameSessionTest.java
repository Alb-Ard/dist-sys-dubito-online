package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.*;
import org.abianchi.dubito.app.gameSession.controllers.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionTest {

    private static final int MAXPLAYERS = 4;

    private final List<Player> testPlayers  = new ArrayList<>();
    private GameSessionController testController;

    @BeforeEach
    void setUp() {
        for(int i = 0; i < MAXPLAYERS; i++) {
            this.testPlayers.add(new PlayerImpl());
        }
        CardTypeFactory.INSTANCE.setSeed(14000);
        this.testController = new GameSessionController(this.testPlayers, Optional.empty());
        this.testController.newRound();
    }

    @Test
    void assertStart() {
        List<Player> sessionPlayers = this.testController.getSessionPlayers();
        GameState gameState = this.testController.getCurrentGameState();
        Assertions.assertEquals(this.testPlayers.get(0), sessionPlayers.get(gameState.getCurrentPlayerIndex()));
        Assertions.assertEquals(-1, gameState.getPreviousPlayerIndex());
        sessionPlayers.forEach(player -> {
            Assertions.assertEquals(player.getHand().size(), Player.MAXHANDSIZE);
            player.getHand().forEach(Assertions::assertNotNull);
        });
    }

    @Test
    void assertPlayAdvanceTurn() {
        GameState currentGameState = this.testController.getCurrentGameState();
        Player currentPlayer = this.testController.getCurrentPlayer();
        this.testController.selectCard(currentPlayer.getHand().get(0));
        this.testController.playCards();
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getCurrentPlayer());
        Assertions.assertEquals(currentPlayer, this.testController.getPreviousPlayer());
    }

    @Test
    void assertPlayCards() {
        GameState currentGameState = this.testController.getCurrentGameState();
        Player currentPlayer = this.testController.getCurrentPlayer();
        List<Card> currentHand = currentPlayer.getHand();
        List<Card> testPlayedCards = new ArrayList<>(currentHand.subList(0, 2));
        for(Card testCard : testPlayedCards) {
            this.testController.selectCard(testCard);
        }
        this.testController.playCards();
        Assertions.assertEquals(currentGameState.getTurnPrevPlayerPlayedCards(), testPlayedCards);
        Assertions.assertEquals(this.testController.getPreviousPlayer().getHand().size(),
                Player.MAXHANDSIZE - testPlayedCards.size());
        testPlayedCards.forEach(card -> Assertions.assertFalse(currentPlayer.getHand().contains(card)));
    }

    @Test
    void assertCallLiar() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue();
        List<Card> currentHand = this.testController.getCurrentPlayer().getHand();
        List<Card> testLiarCards = new ArrayList<>();
        for(Card card : currentHand) {
            if(card.getCardType().getValue() != roundCardType && card.getCardType() != CardType.JOKER) {
                testLiarCards.add(card);
            }
        }
        if(testLiarCards.size() > 3) {
            testLiarCards = new ArrayList<>(testLiarCards.subList(0, 2));
        }
        Player liarPlayer = this.testController.getCurrentPlayer();
        for(Card testCard : testLiarCards) {
            this.testController.selectCard(testCard);
        }
        this.testController.playCards();
        this.testController.callLiar();
        Player callLiarPlayer = this.testController.getPreviousPlayer();
        // check here if the player that call liar hasn't lost a life
        Assertions.assertEquals(2, callLiarPlayer.getLives());
        Assertions.assertEquals(1, liarPlayer.getLives());
        // check if new round has moved the turn
        Assertions.assertEquals(this.testPlayers.get(1), callLiarPlayer);
        Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentPlayer());
    }

    @Test
    void assertWrongCall() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue();
        List<Card> currentHand = this.testController.getCurrentPlayer().getHand();
        List<Card> testTruthCards = new ArrayList<>();
        for(Card card : currentHand) {
            if(card.getCardType().getValue() == roundCardType || card.getCardType() == CardType.JOKER) {
                testTruthCards.add(card);
            }
        }
        if(testTruthCards.size() > 3) {
            testTruthCards = testTruthCards.subList(0, 2).stream().toList();
        }
        Player truthPlayer = this.testController.getCurrentPlayer();
        for(Card testCard : testTruthCards) {
            this.testController.selectCard(testCard);
        }
        this.testController.playCards();
        this.testController.callLiar();
        Player callLiarPlayer = this.testController.getPreviousPlayer();
        // check here if the player that call liar hasn't lost a life
        Assertions.assertEquals(1, callLiarPlayer.getLives());
        Assertions.assertEquals(2, truthPlayer.getLives());
        // check if new round has moved the turn
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getPreviousPlayer());
        Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentPlayer());
    }

    @Test
    void assertCallLiarEmpty() {
        GameState currentGameState = this.testController.getCurrentGameState();
        this.testController.selectCard(this.testController.getCurrentPlayer().getHand().get(0));
        this.testController.playCards();
        this.testController.callLiar();
        Player currentPlayer = this.testController.getCurrentPlayer();
        List<Card> currentPlayerHand = List.copyOf(currentPlayer.getHand());
        this.testController.callLiar();
        Assertions.assertEquals(2, currentPlayer.getLives());
        Assertions.assertEquals(this.testController.getCurrentPlayer(), currentPlayer);
        Assertions.assertIterableEquals(this.testController.getCurrentPlayer().getHand(), currentPlayerHand);
    }


    @Nested
    class gameOverAndNextRoundTest {
        private final List<Player> gameOverPlayers = new ArrayList<>();
        private GameSessionController testGameOverController;


        @Test
        void assertGameOver() {
            for (int i = 0; i < 3; i++) {
                this.gameOverPlayers.add(new PlayerImpl());
            }
            this.gameOverPlayers.get(2).loseLife();
            this.gameOverPlayers.get(2).loseLife();
            this.gameOverPlayers.get(0).loseLife();
            CardTypeFactory.INSTANCE.setSeed(14000);
            this.testGameOverController = new GameSessionController(this.gameOverPlayers, Optional.empty());
            this.testGameOverController.newRound();
            GameState currentGameState = this.testGameOverController.getCurrentGameState();
            CardValue roundCardType = currentGameState.getRoundCardValue();
            List<Card> currentHand = this.testGameOverController.getCurrentPlayer().getHand();
            List<Card> testLiarCards = new ArrayList<>();
            for (Card card : currentHand) {
                if (card.getCardType().getValue() != roundCardType && card.getCardType() != CardType.JOKER) {
                    testLiarCards.add(card);
                }
            }
            if (testLiarCards.size() > 3) {
                testLiarCards = testLiarCards.subList(0, 2).stream().toList();
            }
            for(Card testCard : testLiarCards) {
                this.testGameOverController.selectCard(testCard);
            }
            this.testGameOverController.playCards();
            this.testGameOverController.callLiar();
            Assertions.assertTrue(this.testGameOverController.gameOver(this.testGameOverController.getCurrentPlayer()));
        }

        @Test
        void assertPlayedAllCardsStart() {
            for (int i = 0; i < 2; i++) {
                this.gameOverPlayers.add(new PlayerImpl());
            }
            this.testGameOverController = new GameSessionController(this.gameOverPlayers, Optional.empty());
            this.testGameOverController.newRound();
            Player playerThatWillFinish = this.testGameOverController.getCurrentPlayer();
            List<Card> firstRoundCards = playerThatWillFinish.getHand();
            List<Card> firstPlayedCards = firstRoundCards.subList(0, 3).stream().toList();
            for(Card testCard : firstPlayedCards) {
                this.testGameOverController.selectCard(testCard);
            }
            this.testGameOverController.playCards();
            this.testGameOverController.selectCard(this.testGameOverController.getCurrentPlayer().getHand().get(0));
            this.testGameOverController.playCards();
            for(Card finishPlayerCard : playerThatWillFinish.getHand()) {
                this.testGameOverController.selectCard(finishPlayerCard);
            }
            this.testGameOverController.playCards();
            this.testGameOverController.selectCard(this.testGameOverController.getCurrentPlayer().getHand().get(0));
            this.testGameOverController.playCards();
            List<Card> secondRoundCards = playerThatWillFinish.getHand();
            Assertions.assertEquals(2, this.testGameOverController.getPreviousPlayer().getLives());
            Assertions.assertEquals(2, this.testGameOverController.getCurrentPlayer().getLives());
            Assertions.assertEquals(Player.MAXHANDSIZE, secondRoundCards.size());
            Assertions.assertNotEquals(firstRoundCards, secondRoundCards);
        }
    }
}
