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

    private String cardImagePath;

    private Card card;

    private boolean isClicked;

    private Optional<Rotation> rotation = Optional.empty();

    public CardView(Card card) {
        this.isClicked = false;
        this.card = card;
        switch(this.card.getCardType()){
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
        this.changeImage(cardImagePath);
    }

    private void changeImage(String path) {
        URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(path);
        if(resourceUrl != null) {
            this.cardImagePath = path;
            try {
                BufferedImage originalImage = ImageIO.read(resourceUrl);
                Image correctSizeImage = originalImage.getScaledInstance(70, 120,Image.SCALE_SMOOTH);
                this.setImage(correctSizeImage);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String chooseSeedImage(CardType cardType) {
        return cardType.name().contains("HEARTS") ? "_of_hearts.png" : "_of_spades.png";
    }

    public void click() {this.isClicked = !this.isClicked;}

    public void rotateCard(boolean clockwise) {
        // Set rotation flags
        if (clockwise) {
            this.rotation = Optional.of(Rotation.LEFT);

        } else {
            this.rotation = Optional.of(Rotation.RIGHT);
        }
        this.changeImage(cardImagePath);
        this.applyRotation();
    }

    public void setCardVisibility(boolean visible) {
        String imagePath = visible ? cardImagePath : IMAGE_PATH + "card_back.png";
        this.changeImage(imagePath);
        if(this.rotation.isPresent()) {
            applyRotation();
        }
    }

    private void applyRotation() {
        // Now rotate the image
        Image newImage = this.getImage();
        int width = newImage.getWidth(null);
        int height = newImage.getHeight(null);

        // Create a new buffered image for rotation
        BufferedImage rotatedImage = new BufferedImage(
                height, width, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = rotatedImage.createGraphics();

        // Set up rotation transform
        AffineTransform transform = new AffineTransform();
        transform.translate(height / 2.0, width / 2.0);
        double angle = this.rotation.get() == Rotation.LEFT ? Math.PI / 2 : -Math.PI / 2;
        transform.rotate(angle);
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(newImage, 0, 0, null);
        g2d.dispose();

        // Set the rotated image
        setImage(rotatedImage);
    }

    public String getCardImagePath() {
        return this.cardImagePath;
    }

    public Card getCard() { return this.card; }

    public boolean isClicked() { return this.isClicked; }

}
