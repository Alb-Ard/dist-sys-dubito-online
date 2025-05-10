package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.awt.geom.AffineTransform;
import java.util.Optional;

public class CardView extends ImageIcon {
    private static final String IMAGE_PATH = "card_images/";

    private final Card card;
    private final String cardImagePath;

    private boolean isClicked;
    private Optional<Rotation> rotation;

    public CardView(Card card) {
        this.isClicked = false;
        this.rotation = Optional.empty();
        this.card = card;
        switch (this.card.getCardType()) {
            case ACE_OF_HEARTS, ACE_OF_SPADES:
                cardImagePath = IMAGE_PATH + "ace" + getCardTypeSeedName(card.getCardType());
                break;
            case QUEEN_OF_HEARTS, QUEEN_OF_SPADES:
                cardImagePath = IMAGE_PATH + "queen" + getCardTypeSeedName(card.getCardType());
                break;
            case KING_OF_HEARTS, KING_OF_SPADES:
                cardImagePath = IMAGE_PATH + "king" + getCardTypeSeedName(card.getCardType());
                break;
            default:
                cardImagePath = IMAGE_PATH + "joker_card.png";
                break;
        }
        this.setImage(loadImageFromPath(cardImagePath));
    }

    private static Image loadImageFromPath(String path) {
        if (path == null) {
            return null;
        }
        URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(path);
        if (resourceUrl != null) {
            try {
                BufferedImage loadedImage = ImageIO.read(resourceUrl);
                if (loadedImage == null) {
                    return null;
                }
                Image correctSizeImage = loadedImage.getScaledInstance(70, 120, Image.SCALE_SMOOTH);
                return correctSizeImage;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static String getCardTypeSeedName(CardType cardType) {
        return cardType.name().contains("HEARTS") ? "_of_hearts.png" : "_of_spades.png";
    }

    public void click() {
        this.isClicked = !this.isClicked;
    }

    public void setRotation(Optional<Rotation> rotation) {
        // Set rotation flags
        this.rotation = rotation;
        final Image image = loadImageFromPath(this.cardImagePath);
        this.setImage(this.rotation.map(r -> rotateImage(image, r)).orElse(image));
    }

    public void setCardVisibility(boolean visible) {
        String imagePath = visible ? cardImagePath : IMAGE_PATH + "card_back.png";
        final Image image = loadImageFromPath(imagePath);
        this.setImage(this.rotation.map(r -> rotateImage(image, r)).orElse(image));
    }

    private static Image rotateImage(Image image, Rotation rotation) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        // Create a new buffered image for rotation
        BufferedImage rotatedImage = new BufferedImage(
                height, width, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = rotatedImage.createGraphics();

        // Set up rotation transform
        AffineTransform transform = new AffineTransform();
        transform.translate(height / 2.0, width / 2.0);
        double angle = rotation == Rotation.LEFT ? Math.PI / 2 : -Math.PI / 2;
        transform.rotate(angle);
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // Set the rotated image
        return rotatedImage;
    }

    public String getCardImagePath() {
        return this.cardImagePath;
    }

    public Card getCard() {
        return this.card;
    }

    public boolean isClicked() {
        return this.isClicked;
    }

}
