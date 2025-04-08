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

public class CardView extends ImageIcon {

    private static final String IMAGE_PATH = "card_images/";

    private String cardImagePath;

    private Card card;

    private boolean isClicked;

    private boolean isRotatedLeft = false;
    private boolean isRotatedRight = false;

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
        URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(cardImagePath);
        if(resourceUrl != null) {
            try {
                BufferedImage originalImage = ImageIO.read(resourceUrl);
                Image correctSizeImage = originalImage.getScaledInstance(70, 120,Image.SCALE_SMOOTH);
                this.setImage(correctSizeImage);

            } catch (IOException e) {
                throw new RuntimeException(e);
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
            this.isRotatedLeft = true;
            this.isRotatedRight = false;
        } else {
            this.isRotatedLeft = false;
            this.isRotatedRight = true;
        }

        // Get the current image
        Image currentImage = this.getImage();

        // Convert to BufferedImage to perform rotation
        BufferedImage bufferedImage = new BufferedImage(
                currentImage.getWidth(null),
                currentImage.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        // Draw the current image into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(currentImage, 0, 0, null);
        g2d.dispose();

        // Create a rotated version (90 degrees left or right)
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // The rotated image will have swapped dimensions
        BufferedImage rotatedImage = new BufferedImage(
                height, width, bufferedImage.getType()
        );

        g2d = rotatedImage.createGraphics();

        // Set up the affine transform for rotation
        AffineTransform transform = new AffineTransform();

        // Move to the center of the new image
        transform.translate(height / 2.0, width / 2.0);

        // Rotate 90 degrees clockwise or counter-clockwise
        double angle = clockwise ? Math.PI / 2 : -Math.PI / 2;
        transform.rotate(angle);

        // Move back to adjust for the rotation
        transform.translate(-width / 2.0, -height / 2.0);

        // Apply the transformation
        g2d.setTransform(transform);
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();

        // Set the rotated image as the new image
        this.setImage(rotatedImage);
    }

    /** method to make the card visible or not, by changing it into the card_back.png image */
    public void setCardVisibility(boolean visible) {
        /*
        String imagePath = visible ? cardImagePath : IMAGE_PATH + "card_back.png";
        URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(imagePath);

        if(resourceUrl != null) {
            try {
                BufferedImage originalImage = ImageIO.read(resourceUrl);
                Image correctSizeImage = originalImage.getScaledInstance(70, 120, Image.SCALE_SMOOTH);
                this.setImage(correctSizeImage);

                // Re-apply rotation if needed
                if (isRotatedLeft) {
                    rotateCard(true);
                } else if (isRotatedRight) {
                    rotateCard(false);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } */
        if (visible) {
            // Show the actual card image
            URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(cardImagePath);
            if(resourceUrl != null) {
                try {
                    BufferedImage originalImage = ImageIO.read(resourceUrl);
                    Image correctSizeImage = originalImage.getScaledInstance(70, 120, Image.SCALE_SMOOTH);
                    this.setImage(correctSizeImage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            // Show blank card image (card back)
            URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(IMAGE_PATH + "card_back.png");
            if(resourceUrl != null) {
                try {
                    BufferedImage originalImage = ImageIO.read(resourceUrl);
                    Image correctSizeImage = originalImage.getScaledInstance(70, 120, Image.SCALE_SMOOTH);
                    this.setImage(correctSizeImage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getCardImagePath() {
        return this.cardImagePath;
    }

    public Card getCard() { return this.card; }

    public boolean isClicked() { return this.isClicked; }

}
