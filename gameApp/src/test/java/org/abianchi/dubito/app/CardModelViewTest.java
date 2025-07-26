package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.CardType;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.views.CardView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CardModelViewTest {
    @Test
    void assertSamePath() {
        final Card createdSpecificCard = Card.ofType(CardType.JOKER);
        final CardView createdCardView = new CardView(createdSpecificCard);
        String jokerPath = "card_images/joker_card.png";
        Assertions.assertEquals(jokerPath, createdCardView.getCardImagePath());
    }
}
