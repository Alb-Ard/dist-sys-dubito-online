package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.*;
import org.abianchi.dubito.app.gameSession.controllers.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class GameSessionTest {

    private static final int MAXPLAYERS = 4;
    private static final int MAXHANDSIZE = 5;
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
        Assertions.assertEquals(this.testPlayers.get(0), this.testController.getCurrentTurnPlayer());
        Assertions.assertEquals(this.testPlayers.get(this.testPlayers.size() - 1), this.testController.getPreviousTurnPlayer());
        sessionPlayers.forEach(player -> Assertions.assertEquals(player.getHand().size(), MAXHANDSIZE));
    }

    @Test
    void assertPlayCards() {
        List<Card> currentHand = this.testController.getCurrentTurnPlayer().getHand();
        List<Card> testPlayedCards = currentHand.subList(0, 2).stream().toList();
        Player currentPlayer = this.testController.getCurrentTurnPlayer();
        this.testController.playCards(testPlayedCards);
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getCurrentTurnPlayer());
        Assertions.assertEquals(currentPlayer, this.testController.getPreviousTurnPlayer());
        Assertions.assertEquals(this.testController.getPlayedCards(), testPlayedCards);
        Assertions.assertEquals(this.testController.getPreviousTurnPlayer().getHand().size(), MAXHANDSIZE - testPlayedCards.size());
    }

    @Test
    void assertCallLiar() {
        List<Card> currentHand = this.testController.getCurrentTurnPlayer().getHand();
        List<Card> testLiarCards = new ArrayList<>();
        CARDTYPE turnCardType = this.testController.getTurnCardType();
        for(Card card : currentHand) {
            if(card.getCardType() != turnCardType && card.getCardType() != CARDTYPE.JOKER) {
                testLiarCards.add(card);
            }
        }
        if(testLiarCards.size() > 3) {
            testLiarCards = testLiarCards.subList(0, 2).stream().toList();
        }
        if(testLiarCards.size() > 0) {
            this.testController.playCards(testLiarCards);
            this.testController.checkLiar();
            // check here if the player that call liar hasn't lost a life
            Assertions.assertTrue(this.testController.getPreviousTurnPlayer().getLives() == 2);
            // check if new round has moved the turn
            Assertions.assertEquals(this.testPlayers.get(1), this.testController.getPreviousTurnPlayer());
            Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentTurnPlayer());
        }
    }

    @Test
    void assertWrongCall() {
        List<Card> currentHand = this.testController.getCurrentTurnPlayer().getHand();
        List<Card> testTruthCards = new ArrayList<>();
        CARDTYPE turnCardType = this.testController.getTurnCardType();
        for(Card card : currentHand) {
            if(card.getCardType() == turnCardType || card.getCardType() == CARDTYPE.JOKER) {
                testTruthCards.add(card);
            }
        }
        if(testTruthCards.size() > 3) {
            testTruthCards = testTruthCards.subList(0, 2).stream().toList();
        }
        if(testTruthCards.size() > 0 && testTruthCards.size() <= 3) {
            this.testController.playCards(testTruthCards);
            this.testController.checkLiar();
            Assertions.assertTrue(this.testController.getPreviousTurnPlayer().getLives() < 2);
            Assertions.assertEquals(this.testPlayers.get(1), this.testController.getPreviousTurnPlayer());
            Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentTurnPlayer());
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
            this.gameOverPlayers.get(2).loseRound();
            this.gameOverPlayers.get(2).loseRound();
            this.gameOverPlayers.get(0).loseRound();
            this.testGameOverController = new GameSessionController(this.gameOverPlayers);
            List<Card> currentHand = this.testGameOverController.getCurrentTurnPlayer().getHand();
            List<Card> testLiarCards = new ArrayList<>();
            CARDTYPE turnCardType = this.testGameOverController.getTurnCardType();
            for (Card card : currentHand) {
                if (card.getCardType() != turnCardType && card.getCardType() != CARDTYPE.JOKER) {
                    testLiarCards.add(card);
                }
            }
            if (testLiarCards.size() > 3) {
                testLiarCards = testLiarCards.subList(0, 2).stream().toList();
            }
            if (testLiarCards.size() > 0) {
                this.testGameOverController.playCards(testLiarCards);
                this.testGameOverController.checkLiar();
                Assertions.assertTrue(this.testGameOverController.gameOver(this.testGameOverController.getCurrentTurnPlayer()));
            }
        }
    }
}
