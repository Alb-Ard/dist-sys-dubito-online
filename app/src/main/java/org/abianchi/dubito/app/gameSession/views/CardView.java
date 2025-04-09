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

        this.applyRotation(cardImagePath);
    }

    public void setCardVisibility(boolean visible) {
        String imagePath = visible ? cardImagePath : IMAGE_PATH + "card_back.png";
        if(isRotatedRight || isRotatedLeft) {
            applyRotation(imagePath);
        } else {
            try {
                // Determine which image to show
                URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(imagePath);

                if (resourceUrl != null) {
                    // Load fresh image
                    BufferedImage originalImage = ImageIO.read(resourceUrl);
                    Image correctSizeImage = originalImage.getScaledInstance(70, 120, Image.SCALE_SMOOTH);

                    // Convert to a consistent BufferedImage type
                    BufferedImage bufferedImage = new BufferedImage(
                            correctSizeImage.getWidth(null),
                            correctSizeImage.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB
                    );

                    Graphics2D g2d = bufferedImage.createGraphics();
                    g2d.drawImage(correctSizeImage, 0, 0, null);
                    g2d.dispose();

                    // Set the basic image first
                    setImage(bufferedImage);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void applyRotation(String imagePath) {
        try {
            //  reload the original image and apply all transformations from scratch
            URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(imagePath);
            if (resourceUrl != null) {
                BufferedImage originalImage = ImageIO.read(resourceUrl);
                Image correctSizeImage = originalImage.getScaledInstance(70, 120, Image.SCALE_SMOOTH);

                // Convert to a consistent BufferedImage type
                BufferedImage bufferedImage = new BufferedImage(
                        correctSizeImage.getWidth(null),
                        correctSizeImage.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB
                );

                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(correctSizeImage, 0, 0, null);
                g2d.dispose();

                // Now rotate the image
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                // Create a new buffered image for rotation
                BufferedImage rotatedImage = new BufferedImage(
                        height, width, BufferedImage.TYPE_INT_ARGB
                );

                g2d = rotatedImage.createGraphics();

                // Set up rotation transform
                AffineTransform transform = new AffineTransform();
                transform.translate(height / 2.0, width / 2.0);
                double angle = this.isRotatedLeft ? Math.PI / 2 : -Math.PI / 2;
                transform.rotate(angle);
                transform.translate(-width / 2.0, -height / 2.0);

                g2d.setTransform(transform);
                g2d.drawImage(bufferedImage, 0, 0, null);
                g2d.dispose();

                // Set the rotated image
                setImage(rotatedImage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCardImagePath() {
        return this.cardImagePath;
    }

    public Card getCard() { return this.card; }

    public boolean isClicked() { return this.isClicked; }

}
