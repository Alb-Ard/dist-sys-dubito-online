package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.models.CardType;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardImpl;
import org.abianchi.dubito.app.gameSession.views.CardView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class CardModelViewTest {

    public static void main(String[] args) {

        Card createdSpecificCard = new CardImpl(Optional.of(CardType.JOKER));

        CardView createdCardView = new CardView(createdSpecificCard);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                JLabel label = new JLabel(createdCardView);
                JPanel jPanel = new JPanel();
                jPanel.add(label);
                JFrame frame = new JFrame("Card Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(label);
                frame.setSize(500, 500);
                frame.setVisible(true);
            }
        });
    }
}
