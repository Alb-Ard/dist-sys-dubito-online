package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.models.Card;

import java.awt.*;
import java.util.Random;

public class CardView {

    private static String IMAGE_PATH = "card_images/";
    private Image cardImage;

    public CardView(Card card) {
        String cardImagePath;
        switch(card.getCardType()){
            case ACE:
                cardImagePath = IMAGE_PATH + "ace" + chooseRandomImage();
                break;
            case QUEEN:
                cardImagePath = IMAGE_PATH + "queen" + chooseRandomImage();
                break;
            case KING:
                cardImagePath = IMAGE_PATH + "king" + chooseRandomImage();
                break;
            default:
                cardImagePath = IMAGE_PATH + "joker_card.png";
                break;
        }
        this.cardImage = Toolkit.getDefaultToolkit().getImage(cardImagePath);
    }

    private String chooseRandomImage() {
        Random random = new Random();
        return random.nextBoolean() ? "_of_hearts.png" : "_of_spades.png";
    }

    public Image getCardImage() {
        return this.cardImage;
    }
}
