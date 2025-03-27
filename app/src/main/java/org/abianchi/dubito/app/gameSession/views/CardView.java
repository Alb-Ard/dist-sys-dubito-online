package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardType;

import java.awt.*;
import java.util.Random;

// consigliato fare estensione di CardImage per trasformarlo in un'estensione di swing
public class CardView {

    private static String IMAGE_PATH = "card_images/";
    private Image cardImage;

    public CardView(Card card) {
        String cardImagePath;
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
        this.cardImage = Toolkit.getDefaultToolkit().getImage(cardImagePath);
    }


    private String chooseSeedImage(CardType cardType) {
        return cardType.name().contains("HEARTS") ? "_of_hearts.png" : "_of_spades.png";
    }

    public Image getCardImage() {
        return this.cardImage;
    }
}
