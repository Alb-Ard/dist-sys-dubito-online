package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.CardType;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardImpl;
import org.abianchi.dubito.app.gameSession.views.CardView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CardModelViewTest {

    Card createdSpecificCard;

    CardView createdCardView;

    @Test
    void assertSamePath() {
        this.createdSpecificCard = new CardImpl(Optional.of(CardType.JOKER));
        this.createdCardView = new CardView(createdSpecificCard);
        String jokerPath = "card_images/joker_card.png";
        Assertions.assertEquals(jokerPath, this.createdCardView.getCardImagePath());
    }
}
