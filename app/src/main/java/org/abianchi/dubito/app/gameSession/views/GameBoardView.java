package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class GameBoardView{

    private final Container contentPane;
    private GameSessionController controller;
    private JPanel bottomPlayerCards;
    private JPanel topPlayerCards;
    private JPanel leftPlayerCards;
    private JPanel rightPlayerCards;

    public GameBoardView(GameSessionController controller) {
        this.controller = controller;

        final BorderLayout borderLayout = new BorderLayout();
        JFrame frame = new JFrame("Dubito Online");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.contentPane = frame.getContentPane();
        this.contentPane.setLayout(borderLayout);
        this.contentPane.setPreferredSize(new Dimension(1200, 800));

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
        this.bottomPlayerCards = new JPanel();
        for(Card card : this.controller.getSessionPlayers().get(0).getHand()) {
            JButton buttonCard = getjButton(controller, card, Optional.empty());
            bottomPlayerCards.add(buttonCard);
        }
        this.addButtonsAndLives(bottomPlayerCards, 0,false);
        /** top player */
        this.topPlayerCards = new JPanel();
        for(Card card : this.controller.getSessionPlayers().get(2).getHand()) {
            JButton buttonCard = getjButton(controller, card, Optional.empty());
            topPlayerCards.add(buttonCard);
        }
        this.addButtonsAndLives(topPlayerCards, 2,false);
        /** left player */
        this.leftPlayerCards = new JPanel();
        leftPlayerCards.setLayout(new BoxLayout(leftPlayerCards, BoxLayout.PAGE_AXIS));
        for(Card card : this.controller.getSessionPlayers().get(1).getHand()) {
            JButton buttonCard = getjButton(controller, card, Optional.of("left"));
            leftPlayerCards.add(buttonCard);
        }
        this.addButtonsAndLives(leftPlayerCards, 1, true);
        /** right player */
        this.rightPlayerCards = new JPanel();
        rightPlayerCards.setLayout(new BoxLayout(rightPlayerCards, BoxLayout.Y_AXIS));
        for(Card card : this.controller.getSessionPlayers().get(3).getHand()) {
            JButton buttonCard = getjButton(controller, card, Optional.of("right"));
            rightPlayerCards.add(buttonCard);
        }
        this.addButtonsAndLives(rightPlayerCards, 3, true);


        /**add everything in pane */
        this.contentPane.add(centerPanel, BorderLayout.CENTER);
        this.contentPane.add(bottomPlayerCards, BorderLayout.SOUTH);
        this.contentPane.add(topPlayerCards, BorderLayout.NORTH);
        this.contentPane.add(leftPlayerCards, BorderLayout.WEST);
        this.contentPane.add(rightPlayerCards, BorderLayout.EAST);

        this.updatePlayerTurnUI();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }


    private void updatePlayerTurnUI() {
        // Get current player from controller
        Player currentPlayer = this.controller.getCurrentPlayer();
        int currentPlayerIndex = this.controller.getSessionPlayers().indexOf(currentPlayer);

        // Disable all buttons first
        disableAllPlayerControls();

        // Enable only the current player's controls
        JPanel currentPlayerPanel;
        switch (currentPlayerIndex) {
            case 0: // Bottom player
                enablePanelControls(bottomPlayerCards);
                break;
            case 1: // Left player
                enablePanelControls(leftPlayerCards);
                break;
            case 2: // Top player
                enablePanelControls(topPlayerCards);
                break;
            case 3: // Right player
                enablePanelControls(rightPlayerCards);
                break;
        }
    }

    private void disableAllPlayerControls() {
        disablePanelControls(bottomPlayerCards);
        disablePanelControls(topPlayerCards);
        disablePanelControls(leftPlayerCards);
        disablePanelControls(rightPlayerCards);
    }

    private void disablePanelControls(JPanel panel) {
        // Disable all components in the panel
        for (Component component : panel.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(false);
            } else if (component instanceof JPanel) {
                // For button panels
                for (Component subComponent : ((JPanel) component).getComponents()) {
                    subComponent.setEnabled(false);
                }
            }
        }
    }

    private void enablePanelControls(JPanel panel) {
        // Enable all components in the panel
        for (Component component : panel.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(true);
            } else if (component instanceof JPanel) {
                // For button panels
                for (Component subComponent : ((JPanel) component).getComponents()) {
                    subComponent.setEnabled(true);
                }
            }
        }
    }

    private static JButton getjButton(GameSessionController controller, Card card, Optional<String> rotate) {
        CardView cardView = new CardView(card);
        if(rotate.isPresent()) {
            switch (rotate.get()) {
                case "left":
                    cardView.rotateCard(true);
                    break;
                case "right":
                    cardView.rotateCard(false);
                    break;
            }
        }
        JButton buttonCard = new JButton(cardView);
        buttonCard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!cardView.isClicked()) {
                    cardView.click();
                    controller.selectCard(cardView.getCard());
                } else {
                    controller.removeSelectedCard(cardView.getCard());
                }
            }
        });
        return buttonCard;
    }

    /** This method refreshes the hand for the player that just played some cards*/
    private void refreshPreviousPlayerCards() {
        // Get the player that just played
        Player playerWhoPlayed = this.controller.getPreviousPlayer();

        // Find the player's index to determine which panel to refresh
        int playerIndex = this.controller.getSessionPlayers().indexOf(playerWhoPlayed);

        // Get the appropriate panel based on player's position
        JPanel playerPanel;
        Optional<String> rotateOption = Optional.empty();

        switch (playerIndex) {
            case 1: // Left player
                playerPanel = (JPanel) contentPane.getComponent(3); // WEST
                rotateOption = Optional.of("left");
                break;
            case 2: // Top player
                playerPanel = (JPanel) contentPane.getComponent(2); // NORTH
                break;
            case 3: // Right player
                playerPanel = (JPanel) contentPane.getComponent(4); // EAST
                rotateOption = Optional.of("right");
                break;
            default:
                playerPanel = (JPanel) contentPane.getComponent(1); // SOUTH
                break;
        }

        // Clear the panel
        playerPanel.removeAll();

        // Re-add cards for the player
        for (Card card : playerWhoPlayed.getHand()) {
            JButton buttonCard = getjButton(controller, card, rotateOption);
            playerPanel.add(buttonCard);
        }

        // Re-add buttons with appropriate orientation
        boolean isVertical = (playerIndex == 1 || playerIndex == 3); // Left or right player
        this.addButtonsAndLives(playerPanel, playerIndex, isVertical);

        // Refresh the panel
        playerPanel.revalidate();
        playerPanel.repaint();
        updatePlayerTurnUI();
    }

    /** This method refreshes all the player hands after someone has pressed the call liar button*/
    private void refreshAllPlayerCards() {
        // Refresh bottom player (index 0)
        refreshPlayerPanel(bottomPlayerCards, 0, Optional.empty());

        // Refresh left player (index 1)
        refreshPlayerPanel(leftPlayerCards, 1, Optional.of("left"));

        // Refresh top player (index 2)
        refreshPlayerPanel(topPlayerCards, 2, Optional.empty());

        // Refresh right player (index 3)
        refreshPlayerPanel(rightPlayerCards, 3, Optional.of("right"));

        // Update the center panel with current round card value
        JLabel centerLabel = (JLabel) ((JPanel) contentPane.getComponent(0)).getComponent(1);
        centerLabel.setText("Round Card is: " + this.controller.getCurrentGameState().getRoundCardValue());

        // Update UI to highlight current player's turn
        updatePlayerTurnUI();
    }

    /** Helper method to refresh a specific player's panel */
    private void refreshPlayerPanel(JPanel playerPanel, int playerIndex, Optional<String> rotateOption) {
        // Clear the panel
        playerPanel.removeAll();

        // Re-add cards for the player
        for (Card card : this.controller.getSessionPlayers().get(playerIndex).getHand()) {
            JButton buttonCard = getjButton(controller, card, rotateOption);
            playerPanel.add(buttonCard);
        }

        // Re-add buttons with appropriate orientation
        boolean isVertical = (playerIndex == 1 || playerIndex == 3); // Left or right player
        this.addButtonsAndLives(playerPanel, playerIndex, isVertical);

        // Refresh the panel
        playerPanel.revalidate();
        playerPanel.repaint();
    }

    /** method for the end of the game */
    private void endGame() {
        disableAllPlayerControls();
        // Update the center panel with current round card value
        JLabel centerLabel = (JLabel) ((JPanel) contentPane.getComponent(0)).getComponent(1);
        centerLabel.setText("The winner is: Player " + this.controller.getSessionPlayers().indexOf(this.controller.getCurrentPlayer()));
    }


    private void addButtonsAndLives(JPanel pane,int playerIndex ,boolean vertical) {
        /** buttons and label for player's lives */
        JPanel buttonPanel = new JPanel();
        if(vertical) {
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        }
        JLabel livesLabel = new JLabel("Lives: " + this.controller.getSessionPlayers().get(playerIndex).getLives());
        JButton throwCardsButton = new JButton("Throw Cards (T)");
        JButton callLiarButton = new JButton("Call Liar (F)");
        buttonPanel.add(livesLabel);
        buttonPanel.add(throwCardsButton);
        buttonPanel.add(callLiarButton);

        // Add an action map/input map to the content pane
        JComponent rootComponent = (JComponent) this.contentPane;
        InputMap inputMap = rootComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootComponent.getActionMap();

        // Create actions for throw cards and call liar
        Action throwCardsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!controller.getSelectedCards().isEmpty() && controller.getSelectedCards().size() <= 3) {
                    controller.playCards();
                    refreshPreviousPlayerCards();
                }
            }
        };

        Action callLiarAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.callLiar();
                if(controller.gameOver(controller.getPreviousPlayer()) || controller.gameOver(controller.getCurrentPlayer())) {
                    endGame();
                } else {
                    refreshAllPlayerCards();
                }
            }
        };

        // Bind the T key to the throw cards action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "throwCards");
        actionMap.put("throwCards", throwCardsAction);

        // Bind the F key to the call liar action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "callLiar");
        actionMap.put("callLiar", callLiarAction);

        throwCardsButton.addActionListener(e -> {
            if(!controller.getSelectedCards().isEmpty() && controller.getSelectedCards().size() <= 3) {
                controller.playCards();
                refreshPreviousPlayerCards();
            }
        });

        callLiarButton.addActionListener(e -> {
            controller.callLiar();
            if(controller.gameOver(controller.getPreviousPlayer()) || controller.gameOver(controller.getCurrentPlayer())) {
                endGame();
            } else {
                refreshAllPlayerCards();
            }
        });

        pane.add(buttonPanel);
    }

}
