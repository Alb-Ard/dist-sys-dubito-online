package org.abianchi.dubito.app;

import org.abianchi.dubito.app.models.CARDTYPE;
import org.abianchi.dubito.app.models.Card;
import org.abianchi.dubito.app.models.CardImpl;

import org.abianchi.dubito.app.views.CardView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardModelViewTest {

    private static String IMAGE_PATH = "../../../../../../src/main/java/org/abianchi/dubito/app/resources/";

    final private Card createdSpecificCard = new CardImpl(Optional.of(CARDTYPE.JOKER));
    final private Card createdRandomCard = new CardImpl(Optional.empty());

    @Test
    void testCardModel() {
        Assertions.assertEquals(CARDTYPE.ACE, this.createdSpecificCard.getCardType());
        Assertions.assertTrue(!Optional.of(this.createdRandomCard.getCardType()).isEmpty());
    }

    @Test
    void testCardView() {
        final CardView cardView = new CardView(this.createdSpecificCard);
        final List<Image> possibleImages = new ArrayList<>();
        possibleImages.add(Toolkit.getDefaultToolkit()
                .getImage(IMAGE_PATH + "ace_of_hearts"));
        possibleImages.add(Toolkit.getDefaultToolkit()
                .getImage(IMAGE_PATH + "ace_of_spades"));
        System.out.println("my card: " + cardView.getCardImage());
        System.out.println("card in testList: " + possibleImages.get(0));
        System.out.println("card in testList: " + possibleImages.get(1));
        //Assertions.assertTrue(possibleImages.contains(cardView.getCardImage()));
    }
}
