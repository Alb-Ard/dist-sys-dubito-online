package org.abianchi.dubito.app.gameSession.views;

import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.Player;
import org.albard.dubito.utils.Debouncer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
    private final List<GameBoardPlayerPanel> playerPanels = new ArrayList<>();

    private final JPanel centerPanel;
    private final JLabel roundStateLabel;
    private final JLabel cardsPlayedLabel;

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
        this.roundStateLabel = new JLabel(this.getCurrentRoundStateText());
        this.cardsPlayedLabel = new JLabel("Previous Player played "
                + this.controller.getCurrentGameState().getPreviousPlayerPlayedCards().size() + " cards");
        roundStateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.centerPanel = new JPanel();
        this.centerPanel.setSize(new Dimension(900, 900));
        this.centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        this.centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.centerPanel.add(Box.createVerticalGlue());
        this.centerPanel.add(roundStateLabel);
        this.centerPanel.add(cardsPlayedLabel);
        this.centerPanel.add(Box.createVerticalGlue());
        /* player cards */
        /* panels created based on number of players */
        for (int i = 0; i < nPlayers; i++) {
            final Player player = this.controller.getSessionPlayers().get(i);
            final GameBoardPlayerPanel playerPanel = new GameBoardPlayerPanel(player, PLAYER_ROTATIONS.get(i),
                    this::playCards, this::callLiar);
            this.contentPane.add(playerPanel, PLAYER_POSITIONS.get(i));
            playerPanels.add(playerPanel);
        }

        this.contentPane.add(this.centerPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private String getCurrentRoundStateText() {
        return this.controller.getCurrentGameState().getWinnerPlayerIndex()
                .map(winnerIndex -> "The winner is: Player " + this.controller.getSessionPlayers().get(winnerIndex)
                        .getName().orElse(Integer.toString(winnerIndex)))
                .orElse("Round Card is: " + this.controller.getCurrentGameState().getRoundCardValue()
                        .map(x -> x.toString()).orElse("UNKNOWN"));
    }

    /**
     * This method refreshes all the player hands after someone has pressed either
     * throw cards or the call liar button
     */
    public void refreshBoard() {
        // Refresh players (let the panels synchronize updates)
        for (int i = 0; i < this.playerPanels.size(); i++) {
            this.refreshPlayerPanel(i, false);
        }

        // prima di eseguire un'azione, aspetto un tot di tempo in base al valore
        // passato dentro il Debounce se qualcuno nel frattempo vuole provare ad
        // eseguire la stessa azione, dai precedenza all'ultimo e il mio lavoro viene
        // cancellato
        this.refreshBouncer.Debounce(() -> {
            // provo a fare una lock per il refresh della board, in ogni caso poi rilascio
            // il lavoro di refresh anche in caso ottengo un'eccezione
            if (this.refreshLock.tryLock()) {
                try {
                    if (this.controller.findWinner().isPresent()) {
                        this.endGame();
                        return;
                    }
                    System.out.println(
                            this.controller.getCurrentGameState() + " - " + this.controller.getSessionPlayers());
                    this.roundStateLabel.setText(this.getCurrentRoundStateText());
                } finally {
                    this.refreshLock.unlock();
                }
            }
        });
    }

    /** Helper method to refresh a specific player's panel */
    private void refreshPlayerPanel(final int playerIndex, final boolean forceInactive) {
        this.playerPanels.get(playerIndex).refresh(() -> this.controller.getSessionPlayers().get(playerIndex),
                () -> !forceInactive && this.controller.isActivePlayer(playerIndex));
    }

    /** method for the end of the game */
    private void endGame() {
        for (int i = 0; i < this.playerPanels.size(); i++) {
            this.refreshPlayerPanel(i, true);
        }
        this.roundStateLabel.setText(this.getCurrentRoundStateText());
    }

    private void callLiar() {
        this.controller.callLiar();
        if (this.controller.findWinner().isPresent()) {
            this.endGame();
        } else {
            this.refreshBoard();
        }
    }

    private void playCards(final List<Card> cards) {
        if (!cards.isEmpty() && cards.size() <= 3) {
            this.controller.playCards(cards);
            this.refreshBoard();
        }
    }

    public void setBoardVisible(boolean visible) {
        this.frame.setVisible(visible);
    }
}
