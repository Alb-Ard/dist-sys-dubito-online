package org.abianchi.dubito.app;

import org.abianchi.dubito.app.models.CARDTYPE;
import org.abianchi.dubito.app.models.Card;
import org.abianchi.dubito.app.models.CardImpl;
import org.abianchi.dubito.app.views.CardView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class CardViewApp {

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
                /*
                try {
                    File myObj = new File("card_images/stupidTest.txt");
                    Scanner myReader = new Scanner(myObj);
                    while (myReader.hasNextLine()) {
                        String data = myReader.nextLine();
                        System.out.println(data);
                    }
                    myReader.close();

                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }*/
                try {
                    BufferedImage image = ImageIO.read(new FileInputStream("card_images/joker_card.png"));
                    System.out.println("Image loaded successfully!");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Image img = Toolkit.getDefaultToolkit().getImage("/card_images/joker_card.png");
                JLabel label = new JLabel(new ImageIcon(img));
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
