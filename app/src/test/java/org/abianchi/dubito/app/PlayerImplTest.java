package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerImplTest {

    private static final int MAXHANDSIZE = 5;
    private final Player testPlayer = new PlayerImpl();
    private final List<Card> newHand = new ArrayList<>();

    @BeforeEach
    void setUp() {
        for(int i = 0; i < MAXHANDSIZE ; i ++){
            this.newHand.add(new CardImpl(Optional.empty()));
        }
        this.testPlayer.receiveNewHand(this.newHand);
    }



    @Test
    void playerNewHandTest() {
        Assertions.assertTrue(this.testPlayer.getHand().size() == MAXHANDSIZE);
        final List<Card> differentHand = new ArrayList<>();
        for (int i = 0; i < 5 ; i ++){
            differentHand.add(new CardImpl(Optional.empty()));
        }
        this.testPlayer.receiveNewHand(differentHand);
        Assertions.assertTrue(this.testPlayer.getHand() != this.newHand);
    }

    @Test
    void playerPlayTest() {
        this.testPlayer.playCards(this.newHand.subList(0, 2));
        Assertions.assertTrue(this.testPlayer.getHand().size() < MAXHANDSIZE);
    }

    @Test
    void playerGameOver() {
        this.testPlayer.loseRound();
        Assertions.assertTrue(this.testPlayer.getLives() == 1);
        this.testPlayer.loseRound();
        Assertions.assertTrue(this.testPlayer.getLives() == 0);
    }


}
