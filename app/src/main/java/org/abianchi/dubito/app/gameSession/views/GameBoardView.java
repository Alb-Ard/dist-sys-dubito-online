package org.abianchi.dubito.app.gameSession.views;

import com.jgoodies.binding.value.DefaultComponentValueModel;
import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.albard.dubito.utils.Debouncer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;

public class GameBoardView {
    private static final List<String> PLAYER_POSITIONS = List.of(BorderLayout.SOUTH, BorderLayout.WEST,
            BorderLayout.NORTH, BorderLayout.EAST);
    private static final List<Optional<Rotation>> PLAYER_ROTATIONS = List.of(Optional.empty(),
            Optional.of(Rotation.LEFT), Optional.empty(), Optional.of(Rotation.RIGHT));

    private final Container contentPane;
    private final GameSessionController<?> controller;
    private final Lock refreshLock = new ReentrantLock();
    private final Debouncer refreshBouncer = new Debouncer(Duration.ofMillis(150));

    private final JFrame frame;
    private final List<JPanel> playerCardPanels = new ArrayList<>();

    private final JPanel centerPanel;

    public GameBoardView(GameSessionController<?> controller, String title) {
        this.controller = controller;
        int nPlayers = this.controller.getSessionPlayers().size();

        final BorderLayout borderLayout = new BorderLayout();
        this.frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.contentPane = frame.getContentPane();
        this.contentPane.setLayout(borderLayout);
        this.contentPane.setPreferredSize(new Dimension(1200, 800));

        /** center */
        JLabel roundValueLabel = new JLabel(
                "Round Card is: " + this.controller.getCurrentGameState().getRoundCardValue());
        JLabel cardsPlayedLabel = new JLabel("Previous Player played "
                + this.controller.getCurrentGameState().getTurnPrevPlayerPlayedCards().size() + " cards");
        roundValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.centerPanel = new JPanel();
        this.centerPanel.setSize(new Dimension(900, 900));
        this.centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        this.centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.centerPanel.add(Box.createVerticalGlue());
        this.centerPanel.add(roundValueLabel);
        this.centerPanel.add(cardsPlayedLabel);
        this.centerPanel.add(Box.createVerticalGlue());
        /* player cards */
        /* panels created based on number of players */
        for (int i = 0; i < nPlayers; i++) {
            final JPanel playerCards = new JPanel();
            final Optional<Rotation> rotation = PLAYER_ROTATIONS.get(i);
            playerCards.setLayout(new BoxLayout(playerCards, rotation.map(x -> BoxLayout.PAGE_AXIS).orElse(BoxLayout.LINE_AXIS)));
            for (Card card : this.controller.getSessionPlayers().get(i).getHand()) {
                CardView buttonCard = getCardJButton(controller, card, rotation);
                playerCards.add(buttonCard);
            }
            this.addButtonsAndLives(playerCards, i, rotation.isPresent());
            this.contentPane.add(playerCards, PLAYER_POSITIONS.get(i));
            playerCardPanels.add(playerCards);
        }

        this.contentPane.add(this.centerPanel, BorderLayout.CENTER);

        this.updatePlayerTurnUI();

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void updatePlayerTurnUI() {
        // Disable all buttons first
        disableAllPlayerControls();

        // Hide all card faces
        hideAllCardFaces();

        // Get current player from controller
        int currentPlayerIndex = this.controller.getCurrentGameState().getCurrentPlayerIndex();

        // se il giocatore non è attivo, nasconde tutte le carte nella view
        if (!this.controller.isActivePlayer(currentPlayerIndex)) {
            return;
        }

        // Show only the current player's card faces
        showCardFaces(currentPlayerIndex);

        // Enable only the current player's controls
        setPanelControlsEnabled(playerCardPanels.get(currentPlayerIndex), true);
    }

    /** helper method to disable everything first */
    private void disableAllPlayerControls() {
        playerCardPanels.stream().forEach(el -> setPanelControlsEnabled(el, false));
    }

    /** this method enables control only to the current playing player */
    private void setPanelControlsEnabled(JPanel panel, boolean enabled) {
        // Enable all components in the panel
        for (Component component : panel.getComponents()) {
            if (component instanceof AbstractButton) {
                component.setEnabled(enabled);
            } else if (component instanceof JPanel subPanel) {
                // For button panels
                for (Component subComponent : subPanel.getComponents()) {
                    subComponent.setEnabled(enabled);
                }
            }
        }
    }

    /** helper method to hide all the cards in play */
    private void hideAllCardFaces() {
        playerCardPanels.stream().forEach(el -> setCardFacesVisibility(el, false));
    }

    /** method to show the cards for the current playing player */
    private void showCardFaces(int playerIndex) {
        System.out.println("Shown card for player " + playerIndex);
        setCardFacesVisibility(playerCardPanels.get(playerIndex), true);
    }

    /** this is the method that enables the visibility of the card */
    private void setCardFacesVisibility(JPanel panel, boolean visible) {
        for (Component component : panel.getComponents()) {
            if (component instanceof CardView cardView) {
                cardView.setCardVisibility(visible);
                cardView.repaint();
            }
        }
    }

    /** method used to add buttons for all the cards */
    private static CardView getCardJButton(GameSessionController<?> controller, Card card, Optional<Rotation> rotate) {
        final CardView cardView = new CardView(card);
        cardView.setRotation(rotate);
        cardView.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    controller.selectCard(cardView.getCard());
                } else {
                    controller.removeSelectedCard(cardView.getCard());
                }
            }
        });
        return cardView;
    }

    /**
     * This method refreshes all the player hands after someone has pressed either
     * throw cards or the call liar button
     */
    public void refreshBoard() {
        // prima di eseguire un'azione, aspetto un tot di tempo in base al valore passato dentro il Debounce
        // se qualcuno nel frattempo vuole provare ad eseguire la stessa azione, dai precedenza all'ultimo e il mio lavoro
        // viene cancellato
        this.refreshBouncer.Debounce(() -> {
            // provo a fare una lock per il refresh della board, in ogni caso poi rilascio il lavoro di refresh anche in caso
            // ottengo un'eccezione
            if(this.refreshLock.tryLock()) {
                try {
                    System.out.println(this.controller.getCurrentGameState() + " - " + this.controller.getSessionPlayers());
                    // Refresh players
                    for (int i = 0; i < playerCardPanels.size(); i++) {
                        refreshPlayerPanel(playerCardPanels.get(i), i, PLAYER_ROTATIONS.get(i));
                    }

                    // Update the center panel with current round card value
                    JLabel centerLabel = (JLabel) this.centerPanel.getComponent(1);
                    centerLabel.setText("Round Card is: " + this.controller.getCurrentGameState().getRoundCardValue());

                    // Update UI to highlight current player's turn
                    updatePlayerTurnUI();
                } finally {
                    this.refreshLock.unlock();
                }
            }
        });
    }

    /** Helper method to refresh a specific player's panel */
    private void refreshPlayerPanel(JPanel playerPanel, int playerIndex, Optional<Rotation> rotateOption) {
        // Clear the panel
        playerPanel.removeAll();

        // Re-add cards for the player
        int currentPlayerIndex = this.controller.getCurrentGameState().getCurrentPlayerIndex();
        for (Card card : this.controller.getSessionPlayers().get(playerIndex).getHand()) {
            CardView buttonCard = getCardJButton(controller, card, rotateOption);
            // Set card visibility based on current player
            buttonCard.setCardVisibility(playerIndex == currentPlayerIndex);

            playerPanel.add(buttonCard);
        }

        // Re-add buttons with appropriate orientation
        this.addButtonsAndLives(playerPanel, playerIndex, rotateOption.isPresent());

        // Refresh the panel
        playerPanel.revalidate();
        playerPanel.repaint();
    }

    /** method for the end of the game */
    private void endGame() {
        disableAllPlayerControls();
        // Update the center panel with current round card value
        JLabel centerLabel = (JLabel)this.centerPanel.getComponent(1);
        centerLabel.setText("The winner is: Player " + this.controller.getWinnerIndex());
    }

    private void addButtonsAndLives(JPanel pane, int playerIndex, boolean vertical) {
        /** buttons and label for player's lives */
        JPanel buttonPanel = new JPanel();
        if (vertical) {
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
                if (!controller.getSelectedCards().isEmpty() && controller.getSelectedCards().size() <= 3) {
                    controller.playCards();
                    refreshBoard();
                }
            }
        };

        Action callLiarAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.callLiar();
                if (controller.getPreviousPlayer().map(controller::gameOver).orElse(false)) {
                    endGame();
                } else if (controller.getCurrentPlayer().map(controller::gameOver).orElse(false)) {
                    endGame();
                } else {
                    refreshBoard();
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
            if (!controller.getSelectedCards().isEmpty() && controller.getSelectedCards().size() <= 3) {
                controller.playCards();
                refreshBoard();
            }
        });

        callLiarButton.addActionListener(e -> {
            controller.callLiar();
            if (controller.getPreviousPlayer().map(controller::gameOver).orElse(false)) {
                endGame();
            } else if (controller.getCurrentPlayer().map(controller::gameOver).orElse(false)) {
                endGame();
            } else {
                refreshBoard();
            }
        });

        pane.add(buttonPanel);
    }

    public void setBoardVisible(boolean visible) {
        this.frame.setVisible(visible);
    }

}
