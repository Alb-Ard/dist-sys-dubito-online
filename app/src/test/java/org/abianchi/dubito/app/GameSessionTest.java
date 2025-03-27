package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.*;
import org.abianchi.dubito.app.gameSession.controllers.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameSessionTest {

    private static final int MAXPLAYERS = 4;

    private final List<Player> testPlayers  = new ArrayList<>();
    private GameSessionController testController;

    @BeforeEach
    void setUp() {
        for(int i = 0; i < MAXPLAYERS; i++) {
            this.testPlayers.add(new PlayerImpl());
        }
        this.testController = new GameSessionController(this.testPlayers);
    }

    @Test
    void assertStart() {
        List<Player> sessionPlayers = this.testController.getSessionPlayers();
        GameState gameState = this.testController.getCurrentGameState();
        Assertions.assertEquals(this.testPlayers.get(0), this.testController.getSessionPlayers().get(gameState.getCurrentPlayerIndex()));
        Assertions.assertEquals(-1, gameState.getPreviousPlayerIndex());
        sessionPlayers.forEach(player -> Assertions.assertEquals(player.getHand().size(), Player.MAXHANDSIZE));
    }

    @Test
    void assertPlayCards() {
        GameState currentGameState = this.testController.getCurrentGameState();
        List<Card> currentHand = this.testController.getSessionPlayers().get(currentGameState.getCurrentPlayerIndex()).getHand();
        List<Card> testPlayedCards = new ArrayList<>(currentHand.subList(0, 2));
        Player currentPlayer = this.testController.getSessionPlayers().get(currentGameState.getCurrentPlayerIndex());
        this.testController.playCards(testPlayedCards);
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getSessionPlayers().get(currentGameState.getCurrentPlayerIndex()));
        Assertions.assertEquals(currentPlayer, this.testController.getSessionPlayers().get(currentGameState.getPreviousPlayerIndex()));
        Assertions.assertEquals(currentGameState.getTurnPrevPlayerPlayedCards(), testPlayedCards);
        Assertions.assertEquals(this.testController.getSessionPlayers().get(currentGameState.getPreviousPlayerIndex())
                .getHand().size(), Player.MAXHANDSIZE - testPlayedCards.size());
    }

    @Test
    void assertCallLiar() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue();
        List<Card> currentHand = this.testController.getSessionPlayers().get(currentGameState.getCurrentPlayerIndex()).getHand();
        //List<Card> currentHand = this.testController.getCurrentTurnPlayer().getHand();
        List<Card> testLiarCards = new ArrayList<>();
        for(Card card : currentHand) {
            if(card.getCardType().getValue() != roundCardType && card.getCardType() != CardType.JOKER) {
                testLiarCards.add(card);
            }
        }
        if(testLiarCards.size() > 3) {
            testLiarCards = new ArrayList<>(testLiarCards.subList(0, 2));
        }
        if(testLiarCards.size() > 0) {
            this.testController.playCards(testLiarCards);
            this.testController.checkLiar();
            // check here if the player that call liar hasn't lost a life
            Assertions.assertTrue(this.testController.getSessionPlayers().get(currentGameState
                    .getPreviousPlayerIndex()).getLives() == 2);
            // check if new round has moved the turn
            Assertions.assertEquals(this.testPlayers.get(1), this.testController.getSessionPlayers().get(currentGameState
                    .getPreviousPlayerIndex()));
            Assertions.assertEquals(this.testPlayers.get(2), this.testController.getSessionPlayers().get(currentGameState
                    .getCurrentPlayerIndex()));
        }
    }

    @Test
    void assertWrongCall() {
        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue();
        List<Card> currentHand = this.testController.getSessionPlayers().get(currentGameState.getCurrentPlayerIndex()).getHand();
        List<Card> testTruthCards = new ArrayList<>();
        for(Card card : currentHand) {
            if(card.getCardType().getValue() == roundCardType || card.getCardType() == CardType.JOKER) {
                testTruthCards.add(card);
            }
        }
        if(testTruthCards.size() > 3) {
            testTruthCards = testTruthCards.subList(0, 2).stream().toList();
        }
        if(testTruthCards.size() > 0 && testTruthCards.size() <= 3) {
            this.testController.playCards(testTruthCards);
            this.testController.checkLiar();
            // check here if the player that call liar hasn't lost a life
            Assertions.assertTrue(this.testController.getSessionPlayers().get(currentGameState
                    .getPreviousPlayerIndex()).getLives() < 2);
            // check if new round has moved the turn
            Assertions.assertEquals(this.testPlayers.get(1), this.testController.getSessionPlayers().get(currentGameState
                    .getPreviousPlayerIndex()));
            Assertions.assertEquals(this.testPlayers.get(2), this.testController.getSessionPlayers().get(currentGameState
                    .getCurrentPlayerIndex()));
        }
    }


    @Nested
    class gameOverTest {
        private final List<Player> gameOverPlayers = new ArrayList<>();
        private GameSessionController testGameOverController;


        @Test
        void gameOver() {
            for (int i = 0; i < 3; i++) {
                this.gameOverPlayers.add(new PlayerImpl());
            }
            this.gameOverPlayers.get(2).loseLife();
            this.gameOverPlayers.get(2).loseLife();
            this.gameOverPlayers.get(0).loseLife();
            this.testGameOverController = new GameSessionController(this.gameOverPlayers);
            GameState currentGameState = this.testGameOverController.getCurrentGameState();
            CardValue roundCardType = currentGameState.getRoundCardValue();
            List<Card> currentHand = this.testGameOverController.getSessionPlayers().get(currentGameState.getCurrentPlayerIndex()).getHand();
            List<Card> testLiarCards = new ArrayList<>();
            for (Card card : currentHand) {
                if (card.getCardType().getValue() != roundCardType && card.getCardType() != CardType.JOKER) {
                    testLiarCards.add(card);
                }
            }
            if (testLiarCards.size() > 3) {
                testLiarCards = testLiarCards.subList(0, 2).stream().toList();
            }
            if (testLiarCards.size() > 0) {
                this.testGameOverController.playCards(testLiarCards);
                this.testGameOverController.checkLiar();
                Assertions.assertTrue(this.testGameOverController.gameOver(this.testGameOverController.getSessionPlayers()
                        .get(currentGameState.getCurrentPlayerIndex())));
            }
        }
    }
}
