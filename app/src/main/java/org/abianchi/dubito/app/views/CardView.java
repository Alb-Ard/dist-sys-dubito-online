package org.abianchi.dubito.app.views;

import org.abianchi.dubito.app.models.Card;

import java.awt.*;
import java.util.Random;

public class CardView {

    private static String IMAGE_PATH = "../resources/";
    private Image cardImage;

    public CardView(Card card) {
        String cardImage;
        switch(card.getCardType()){
            case ACE:
                cardImage = IMAGE_PATH + "ace" + chooseRandomImage();
                break;
            case QUEEN:
                cardImage = IMAGE_PATH + "queen" + chooseRandomImage();
                break;
            case KING:
                cardImage = IMAGE_PATH + "king" + chooseRandomImage();
                break;
            default:
                cardImage = IMAGE_PATH + "joker_card.png";
                break;
        }
        this.cardImage = Toolkit.getDefaultToolkit().getImage(cardImage);
    }

    private String chooseRandomImage() {
        Random random = new Random();
        return random.nextBoolean() ? "_of_hearts.png" : "_of_spades.png";
    }
}
