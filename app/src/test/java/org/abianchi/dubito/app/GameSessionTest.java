package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.*;
import org.abianchi.dubito.app.gameSession.controllers.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class GameSessionTest {

    private static final int MAXPLAYERS = 4;

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
        Assertions.assertEquals(this.testPlayers.get(0), sessionPlayers.get(gameState.getCurrentPlayerIndex().get()));
        Assertions.assertEquals(-1, gameState.getPreviousPlayerIndex().get());
        sessionPlayers.forEach(player -> {
            Assertions.assertEquals(player.getHand().size(), Player.MAX_HAND_SIZE);
            player.getHand().forEach(Assertions::assertNotNull);
        });
    }

    @Test
    void assertPlayAdvanceTurn() {
        Player currentPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(List.of(currentPlayer.getHand().get(0)));
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getCurrentPlayer().get());
        Assertions.assertEquals(currentPlayer, this.testController.getPreviousPlayer().get());
    }

    @Test
    void assertPlayCards() {
        GameState currentGameState = this.testController.getCurrentGameState();
        Player currentPlayer = this.testController.getCurrentPlayer().get();
        List<Card> currentHand = currentPlayer.getHand();
        List<Card> testPlayedCards = new ArrayList<>(currentHand.subList(0, 2));
        this.testController.playCards(testPlayedCards);
        Assertions.assertEquals(currentGameState.getPreviousPlayerPlayedCards(), testPlayedCards);
        Assertions.assertEquals(this.testController.getPreviousPlayer().get().getHand().size(),
                Player.MAX_HAND_SIZE - testPlayedCards.size());
        // testPlayedCards.forEach(card ->
        // Assertions.assertFalse(currentPlayer.getHand().contains(card)));
    }

    @Test
    void assertCallLiar() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue().get();
        List<Card> currentHand = this.testController.getCurrentPlayer().get().getHand();
        List<Card> testLiarCards = new ArrayList<>();
        for (Card card : currentHand) {
            if (card.getCardType().getValue() != roundCardType && card.getCardType() != CardType.JOKER) {
                testLiarCards.add(card);
            }
        }
        if (testLiarCards.size() > 3) {
            testLiarCards = new ArrayList<>(testLiarCards.subList(0, 2));
        }
        Player liarPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(testLiarCards);
        this.testController.callLiar();
        Player callLiarPlayer = this.testController.getPreviousPlayer().get();
        // check here if the player that call liar hasn't lost a life
        Assertions.assertEquals(2, callLiarPlayer.getLives());
        Assertions.assertEquals(1, liarPlayer.getLives());
        // check if new round has moved the turn
        Assertions.assertEquals(this.testPlayers.get(1), callLiarPlayer);
        Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentPlayer().get());
    }

    @Test
    void assertWrongCall() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue().get();
        List<Card> currentHand = this.testController.getCurrentPlayer().get().getHand();
        List<Card> testTruthCards = new ArrayList<>();
        for (Card card : currentHand) {
            if (card.getCardType().getValue() == roundCardType || card.getCardType() == CardType.JOKER) {
                testTruthCards.add(card);
            }
        }
        if (testTruthCards.size() > 3) {
            testTruthCards = testTruthCards.subList(0, 2).stream().toList();
        }
        Player truthPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(testTruthCards);
        this.testController.callLiar();
        Player callLiarPlayer = this.testController.getPreviousPlayer().get();
        // check here if the player that call liar hasn't lost a life
        Assertions.assertEquals(1, callLiarPlayer.getLives());
        Assertions.assertEquals(2, truthPlayer.getLives());
        // check if new round has moved the turn
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getPreviousPlayer().get());
        Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentPlayer().get());
    }

    @Test
    void assertCallLiarEmpty() {
        this.testController.playCards(List.of(this.testController.getCurrentPlayer().get().getHand().get(0)));
        this.testController.callLiar();
        Player currentPlayer = this.testController.getCurrentPlayer().get();
        List<Card> currentPlayerHand = List.copyOf(currentPlayer.getHand());
        this.testController.callLiar();
        Assertions.assertEquals(2, currentPlayer.getLives());
        Assertions.assertEquals(this.testController.getCurrentPlayer().get(), currentPlayer);
        Assertions.assertIterableEquals(this.testController.getCurrentPlayer().get().getHand(), currentPlayerHand);
    }

    @Nested
    class gameOverAndNextRoundTest {
        private final List<Player> gameOverPlayers = new ArrayList<>();
        private GameSessionController<Player> testGameOverController;

        @Test
        void assertGameOver() {
            for (int i = 0; i < 3; i++) {
                this.gameOverPlayers.add(new PlayerImpl());
            }
            this.gameOverPlayers.get(2).loseLife();
            this.gameOverPlayers.get(2).loseLife();
            this.gameOverPlayers.get(0).loseLife();
            CardTypeFactory.INSTANCE.setSeed(14000);
            this.testGameOverController = new GameSessionController<>(this.gameOverPlayers);
            this.testGameOverController.newRound();
            GameState currentGameState = this.testGameOverController.getCurrentGameState();
            CardValue roundCardType = currentGameState.getRoundCardValue().get();
            List<Card> currentHand = this.testGameOverController.getCurrentPlayer().get().getHand();
            List<Card> testLiarCards = new ArrayList<>();
            for (Card card : currentHand) {
                if (card.getCardType().getValue() != roundCardType && card.getCardType() != CardType.JOKER) {
                    testLiarCards.add(card);
                }
            }
            if (testLiarCards.size() > 3) {
                testLiarCards = testLiarCards.subList(0, 2).stream().toList();
            }
            this.testGameOverController.playCards(testLiarCards);
            this.testGameOverController.callLiar();
            Assertions.assertTrue(this.testGameOverController.findWinner().isPresent());
            Assertions.assertEquals(this.testGameOverController.findWinner().get(),
                    this.testGameOverController.getCurrentPlayer().get());
        }

        @Test
        void assertPlayedAllCardsStart() {
            for (int i = 0; i < 2; i++) {
                this.gameOverPlayers.add(new PlayerImpl());
            }
            this.testGameOverController = new GameSessionController<>(this.gameOverPlayers);
            this.testGameOverController.newRound();
            Player playerThatWillFinish = this.testGameOverController.getCurrentPlayer().get();
            List<Card> firstRoundCards = playerThatWillFinish.getHand();
            List<Card> firstPlayedCards = firstRoundCards.subList(0, 3).stream().toList();
            this.testGameOverController.playCards(firstPlayedCards);
            this.testGameOverController
                    .playCards(List.of(this.testGameOverController.getCurrentPlayer().get().getHand().get(0)));
            this.testGameOverController.playCards(playerThatWillFinish.getHand());
            this.testGameOverController
                    .playCards(List.of(this.testGameOverController.getCurrentPlayer().get().getHand().get(0)));
            List<Card> secondRoundCards = playerThatWillFinish.getHand();
            Assertions.assertEquals(2, this.testGameOverController.getPreviousPlayer().get().getLives());
            Assertions.assertEquals(2, this.testGameOverController.getCurrentPlayer().get().getLives());
            Assertions.assertEquals(Player.MAX_HAND_SIZE, secondRoundCards.size());
            Assertions.assertNotEquals(firstRoundCards, secondRoundCards);
        }
    }
}
