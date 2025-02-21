package org.abianchi.dubito.app;

import org.abianchi.dubito.app.models.CARDTYPE;
import org.abianchi.dubito.app.models.Card;
import org.abianchi.dubito.app.models.CardImpl;

import org.abianchi.dubito.app.views.CardView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardModelViewTest {

    public static void main(String[] args) {

        Card createdSpecificCard = new CardImpl(Optional.of(CARDTYPE.JOKER));

        CardView createdCardView = new CardView(createdSpecificCard);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                String path = "card_images/joker_card.png";
                final URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(path);
                if( resourceUrl != null ) {
                    try {
                        BufferedImage image = ImageIO.read(resourceUrl);
                        JLabel label = new JLabel(new ImageIcon(image));
                        JPanel jPanel = new JPanel();
                        jPanel.add(label);
                        JFrame frame = new JFrame("Card Test");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setContentPane(label);
                        frame.setSize(500, 500);
                        frame.setVisible(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
