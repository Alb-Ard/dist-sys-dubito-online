package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardType;
import org.albard.utils.Logger;

import javax.swing.*;
import java.util.Optional;

public class CardView extends JToggleButton {
    private static final String IMAGE_FOLDER_PATH = "card_images/";
    private static final String CARD_BACK_IMAGE_FILE_PATH = IMAGE_FOLDER_PATH + "card_back.png";

    private final Card card;
    private final String cardImagePath;

    private Optional<Rotation> rotation;

    public CardView(final Card card) {
        this.rotation = Optional.empty();
        this.card = card;
        switch (this.card.getCardType()) {
            case ACE_OF_HEARTS, ACE_OF_SPADES:
                this.cardImagePath = IMAGE_FOLDER_PATH + "ace" + getCardTypeSeedName(card.getCardType());
                break;
            case QUEEN_OF_HEARTS, QUEEN_OF_SPADES:
                this.cardImagePath = IMAGE_FOLDER_PATH + "queen" + getCardTypeSeedName(card.getCardType());
                break;
            case KING_OF_HEARTS, KING_OF_SPADES:
                this.cardImagePath = IMAGE_FOLDER_PATH + "king" + getCardTypeSeedName(card.getCardType());
                break;
            default:
                this.cardImagePath = IMAGE_FOLDER_PATH + "joker_card.png";
                break;
        }
        this.setImageFromPath(this.cardImagePath);
    }

    private static String getCardTypeSeedName(CardType cardType) {
        return cardType.name().contains("HEARTS") ? "_of_hearts.png" : "_of_spades.png";
    }

    public void setRotation(Optional<Rotation> rotation) {
        // Set rotation flags
        this.rotation = rotation;
        this.setImageFromPath(this.cardImagePath);
    }

    public void setCardVisibility(boolean visible) {
        final String imagePath = visible ? cardImagePath : CARD_BACK_IMAGE_FILE_PATH;
        this.setImageFromPath(imagePath);
    }

    private void setImageFromPath(final String imagePath) {
        ImageUtilities.loadImageFromPath(imagePath, 70, 150).ifPresentOrElse(image -> SwingUtilities.invokeLater(() -> {
            try {
                this.setIcon(new ImageIcon(this.rotation.map(r -> ImageUtilities.rotateImage(image, r)).orElse(image)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }), () -> Logger.logError("Image not found: " + imagePath));
    }

    public String getCardImagePath() {
        return this.cardImagePath;
    }

    public Card getCard() {
        return this.card;
    }
}
