package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GameBoardView{

    private GameSessionController controller;
    public GameBoardView(GameSessionController controller) {
        this.controller = controller;

        final BorderLayout borderLayout = new BorderLayout();
        JFrame frame = new JFrame("Dubito Online");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final Container contentPane = frame.getContentPane();
        contentPane.setLayout(borderLayout);
        contentPane.setPreferredSize(new Dimension(800, 800));

        /**  center */
        JLabel centerLabel = new JLabel("Round Card is: " + this.controller.getCurrentGameState().getRoundCardValue());
        centerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel centerPanel = new JPanel();
        centerPanel.setSize(new Dimension(900, 900));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(centerLabel);
        centerPanel.add(Box.createVerticalGlue());
        /**  player cards */
        /** bottom player */
        JPanel bottomPlayerCards = new JPanel();
        for(Card card : this.controller.getSessionPlayers().get(0).getHand()) {
            CardView cardView = new CardView(card);
            JButton buttonCard = new JButton(cardView);
            buttonCard.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.selectCard(cardView.getCard());
                }
            });
            bottomPlayerCards.add(buttonCard);
        }
        /** top player */
        JPanel topPlayerCards = new JPanel();
        for(Card card : this.controller.getSessionPlayers().get(2).getHand()) {
            JLabel labelCard = new JLabel(new CardView(card));
            topPlayerCards.add(labelCard);
        }
        /** left player */
        JPanel leftPlayerCards = new JPanel();
        leftPlayerCards.setLayout(new BoxLayout(leftPlayerCards, BoxLayout.PAGE_AXIS));
        for(Card card : this.controller.getSessionPlayers().get(1).getHand()) {
            CardView cardView = new CardView(card);
            cardView.rotateCard(true);
            JLabel labelCard = new JLabel(cardView);
            leftPlayerCards.add(labelCard);
        }
        /** right player */
        JPanel rightPlayerCards = new JPanel();
        rightPlayerCards.setLayout(new BoxLayout(rightPlayerCards, BoxLayout.Y_AXIS));
        for(Card card : this.controller.getSessionPlayers().get(3).getHand()) {
            CardView cardView = new CardView(card);
            cardView.rotateCard(false);
            JLabel labelCard = new JLabel(cardView);
            rightPlayerCards.add(labelCard);
        }

        /** buttons for player */
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton throwCardsButton = new JButton("Throw Cards (T)");
        JButton callLiarButton = new JButton("Call Liar (F)");
        buttonPanel.add(throwCardsButton);
        buttonPanel.add(callLiarButton);


        throwCardsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.playCards();
            }
        });

        callLiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.callLiar();
            }
        });

        /*
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(topPlayerCards);
        bottomPanel.add(buttonPanel);
        */


        /**add everything in pane */
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(bottomPlayerCards, BorderLayout.SOUTH);
        //contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(topPlayerCards, BorderLayout.NORTH);
        contentPane.add(leftPlayerCards, BorderLayout.WEST);
        contentPane.add(rightPlayerCards, BorderLayout.EAST);


        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

}
