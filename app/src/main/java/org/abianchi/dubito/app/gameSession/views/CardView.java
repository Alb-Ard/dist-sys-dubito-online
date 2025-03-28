package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class CardView extends ImageIcon {

    private static String IMAGE_PATH = "card_images/";

    private String cardImagePath;

    public CardView(Card card) {
        switch(card.getCardType()){
            case ACE_OF_HEARTS, ACE_OF_SPADES:
                cardImagePath = IMAGE_PATH + "ace" + chooseSeedImage(card.getCardType());
                break;
            case QUEEN_OF_HEARTS, QUEEN_OF_SPADES:
                cardImagePath = IMAGE_PATH + "queen" + chooseSeedImage(card.getCardType());
                break;
            case KING_OF_HEARTS, KING_OF_SPADES:
                cardImagePath = IMAGE_PATH + "king" + chooseSeedImage(card.getCardType());
                break;
            default:
                cardImagePath = IMAGE_PATH + "joker_card.png";
                break;
        }
        URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(cardImagePath);
        if(resourceUrl != null) {
            try {
                BufferedImage originalImage = ImageIO.read(resourceUrl);
                Image correctSizeImage = originalImage.getScaledInstance(90, 150,Image.SCALE_SMOOTH);
                this.setImage(correctSizeImage);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String chooseSeedImage(CardType cardType) {
        return cardType.name().contains("HEARTS") ? "_of_hearts.png" : "_of_spades.png";
    }

    public String getCardImagePath() {
        return this.cardImagePath;
    }

}
