package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.*;
import org.abianchi.dubito.app.gameSession.controllers.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSessionTest {

    private static final int MAXPLAYERS = 4;

    private final List<Player> testPlayers = new ArrayList<>();
    private GameSessionController<Player> testController;


    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertStart(int players) {
        setUpController(players);

        List<Player> sessionPlayers = this.testController.getSessionPlayers();
        GameState gameState = this.testController.getCurrentGameState();
        Assertions.assertEquals(this.testPlayers.get(0), sessionPlayers.get(gameState.getCurrentPlayerIndex().get()));
        Assertions.assertTrue(gameState.getPreviousPlayerIndex().isEmpty());
        sessionPlayers.forEach(player -> {
            Assertions.assertEquals(player.getHand().size(), Player.MAX_HAND_SIZE);
            player.getHand().forEach(Assertions::assertNotNull);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertPlayAdvanceTurn(int players) {
        setUpController(players);

        Player currentPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(List.of(currentPlayer.getHand().get(0)));
        Assertions.assertEquals(this.testPlayers.get(1), this.testController.getCurrentPlayer().get());
        Assertions.assertEquals(currentPlayer, this.testController.getPreviousPlayer().get());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertPlayCards(int players) {
        setUpController(players);

        GameState currentGameState = this.testController.getCurrentGameState();
        Player currentPlayer = this.testController.getCurrentPlayer().get();
        List<Card> currentHand = currentPlayer.getHand();
        List<Card> testPlayedCards = new ArrayList<>(currentHand.subList(0, 2));
        this.testController.playCards(testPlayedCards);
        Assertions.assertEquals(currentGameState.getPreviousPlayerPlayedCards(), testPlayedCards);
        Assertions.assertEquals(this.testController.getPreviousPlayer().get().getHand().size(),
                Player.MAX_HAND_SIZE - testPlayedCards.size());
        Assertions.assertEquals(1, currentGameState.getCurrentPlayerIndex().get());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertCallLiar(int players) {
        setUpController(players);

        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue().get();
        List<Card> currentHand = this.testController.getCurrentPlayer().get().getHand();
        List<Card> testLiarCards = getLiarCards(currentHand, roundCardType);
        Player liarPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(testLiarCards);
        this.testController.callLiar();
        Player callLiarPlayer = this.testController.getPreviousPlayer().get();
        // check here if the player that call liar hasn't lost a life
        Assertions.assertEquals(2, callLiarPlayer.getLives());
        Assertions.assertEquals(1, liarPlayer.getLives());
        // check if new round has moved the turn
        Assertions.assertFalse(this.testController.getWinnerPlayer().isPresent());
        if(players == 2) {
            Assertions.assertEquals(callLiarPlayer, this.testController.getPreviousPlayer().get());
            Assertions.assertEquals(liarPlayer, this.testController.getCurrentPlayer().get());
        } else {
            Assertions.assertEquals(this.testPlayers.get(1), callLiarPlayer);
            Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentPlayer().get());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertWrongCall(int players) {
        setUpController(players);

        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundCardType = currentGameState.getRoundCardValue().get();
        List<Card> currentHand = this.testController.getCurrentPlayer().get().getHand();
        List<Card> testTruthCards = getTruthCards(currentHand, roundCardType);
        Player truthPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(testTruthCards);
        this.testController.callLiar();
        Player callLiarPlayer = this.testController.getPreviousPlayer().get();
        // check here if the player that call liar hasn't lost a life
        Assertions.assertEquals(1, callLiarPlayer.getLives());
        Assertions.assertEquals(2, truthPlayer.getLives());
        // check if new round has moved the turn
        Assertions.assertFalse(this.testController.getWinnerPlayer().isPresent());
        if(players == 2) {
            Assertions.assertEquals(callLiarPlayer, this.testController.getPreviousPlayer().get());
            Assertions.assertEquals(truthPlayer, this.testController.getCurrentPlayer().get());
        } else {
            Assertions.assertEquals(this.testPlayers.get(1), callLiarPlayer);
            Assertions.assertEquals(this.testPlayers.get(2), this.testController.getCurrentPlayer().get());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertCallLiarEmpty(int players) {
        setUpController(players);

        // i make the first player play a card, then call liar
        Player firstPlayer = this.testController.getCurrentPlayer().get();
        this.testController.playCards(List.of(firstPlayer.getHand().get(0)));
        this.testController.callLiar();
        // the second player tries to do the call liar, but he can't cause no one played before him
        Player currentPlayer = this.testController.getCurrentPlayer().get();
        List<Card> currentPlayerHand = List.copyOf(currentPlayer.getHand());
        this.testController.callLiar();
        if(players == 2) {
            Assertions.assertEquals(currentPlayer, firstPlayer);
            Assertions.assertEquals(1, currentPlayer.getLives());
            Assertions.assertEquals(this.testController.getCurrentPlayer().get(), firstPlayer);
        } else {
            Assertions.assertEquals(2, currentPlayer.getLives());
            Assertions.assertEquals(this.testController.getCurrentPlayer().get(), currentPlayer);
        }
        Assertions.assertIterableEquals(this.testController.getCurrentPlayer().get().getHand(), currentPlayerHand);
        Assertions.assertEquals(5, currentPlayerHand.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertPlayedAllCardsAndNewRoundStart(int players) {
        setUpController(players);

        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue roundOneValue = currentGameState.getRoundCardValue().get();
        Map<Player, List<Card>> playerHandRound1 = new HashMap<>();

        // i save the hand for each player in Round 1
        for(Player player : this.testController.getSessionPlayers()) {
            playerHandRound1.put(player, player.getHand());
        }

        //i make every player play all their cards
        for (int i = 0; i < (2 * players); i++) {
            List<Card> currentPlayerHand = this.testController.getCurrentPlayer().get().getHand();
            if(currentPlayerHand.size() > 3) {
                this.testController.playCards(currentPlayerHand.subList(0, 3).stream().toList());
            } else {
                this.testController.playCards(currentPlayerHand);
            }
        }

        // checking here that every player did not lose a life, that there is a new round card value and that they all
        // have a different hand now
        for(Player player : this.testController.getSessionPlayers()) {
            Assertions.assertEquals(2, player.getLives());
            Assertions.assertNotEquals(playerHandRound1.get(player), player.getHand());
        }
        Assertions.assertNotEquals(roundOneValue, currentGameState.getRoundCardValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, MAXPLAYERS})
    void assertPlayerLeavesGameContinues(int players) {
        setUpController(players);

        GameState currentGameState = this.testController.getCurrentGameState();
        CardValue currentCardValue = currentGameState.getRoundCardValue().get();

        Player player1 = this.testController.getCurrentPlayer().get();
        Player player3 = this.testController.getSessionPlayers().get(2);
        List<Card> player1Hand = player1.getHand();
        List<Card> player1FirstPlayedCards = player1Hand.subList(0, 3).stream().toList();
        this.testController.playCards(player1FirstPlayedCards); // i make player 1 play his cards, now it's player 2's turn

        // player 2 decides to leave, so i "remove him" from play
        this.testController.removePlayer(currentGameState.getCurrentPlayerIndex().get());

        // check that it is now player 3's turn, that the previous player (aka player 2) is now "removed" (has 0 lives),
        // that there is still no winners and that we are in the same round (didn't move into next round)
        Assertions.assertEquals(player3, this.testController.getCurrentPlayer().get());
        Assertions.assertEquals(0, this.testController.getPreviousPlayer().get().getLives());
        Assertions.assertTrue(currentGameState.getWinnerPlayerIndex().isEmpty());
        Assertions.assertEquals(currentCardValue, currentGameState.getRoundCardValue().get());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, MAXPLAYERS})
    void assertGameOver(int players) {
        setUpController(players);

        GameState currentGameState = this.testController.getCurrentGameState();

        //i make every player play all their cards
        while(currentGameState.getWinnerPlayerIndex().isEmpty()) {
            CardValue currentCardValue = currentGameState.getRoundCardValue().get();
            List<Card> currentPlayerHand = this.testController.getCurrentPlayer().get().getHand();
            List<Card> liarCards = getLiarCards(currentPlayerHand, currentCardValue);
            this.testController.playCards(liarCards);
            this.testController.callLiar();
        }

        // check that there is a winner, and that every other player that has "lost" has 0 lives
        Assertions.assertTrue(currentGameState.getWinnerPlayerIndex().isPresent());
        for(Player player : this.testController.getSessionPlayers()) {
            if(player != this.testController.findWinner().get()) {
                Assertions.assertEquals(0, player.getLives());
            }
        }
    }

    private void setUpController(int players) {
        for (int i = 0; i < players; i++) {
            this.testPlayers.add(new PlayerImpl());
        }
        CardTypeFactory.INSTANCE.setSeed(14000);
        this.testController = new GameSessionController<>(this.testPlayers);
        this.testController.newRound();
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

    private List<Card> getTruthCards(List<Card> playerHand, CardValue roundCardValue) {
        List<Card> testTruthCards = new ArrayList<>();
        for (Card card : playerHand) {
            if (card.getCardType().getValue() == roundCardValue || card.getCardType() == CardType.JOKER) {
                testTruthCards.add(card);
            }
        }
        if (testTruthCards.size() > 3) {
            testTruthCards = new ArrayList<>(testTruthCards.subList(0, 2));
        }
        return testTruthCards;
    }
}
