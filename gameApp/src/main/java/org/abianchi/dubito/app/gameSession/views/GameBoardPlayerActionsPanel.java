package org.abianchi.dubito.app.gameSession.views;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

final class GameBoardPlayerActionsPanel extends JPanel {
    private final JLabel livesLabel;
    private final JLabel playerNameLabel;
    private final JButton callLiarButton;
    private final JButton throwCardsButton;

    private volatile boolean canCallLiar = false;
    private volatile boolean canThrowCards = false;

    public GameBoardPlayerActionsPanel(final int initialLivesCount, final boolean vertical, final String playerName,
            final Runnable playCardsListener, final Runnable callLiarListener) {
        /** buttons and label for player's lives */
        if (vertical) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        this.livesLabel = new JLabel(getLivesText(initialLivesCount));
        this.playerNameLabel = new JLabel(playerName.length() > 12 ? playerName.substring(0, 12) + "..." : playerName);
        this.throwCardsButton = new GameButton("Throw Cards (T)");
        this.callLiarButton = new GameButton("Call Liar (F)");
        this.add(livesLabel);
        this.add(playerNameLabel);
        this.add(throwCardsButton);
        this.add(callLiarButton);

        // Add an action map/input map to the content pane
        final InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final ActionMap actionMap = this.getActionMap();

        // Create wrapper actions for throw cards and call liar listeners
        final Action throwCardsAction = createAction(() -> {
            if (this.canThrowCards) {
                playCardsListener.run();
            }
        });

        final Action callLiarAction = createAction(() -> {
            if (this.canCallLiar) {
                callLiarListener.run();
            }
        });

        // Bind the T key to the throw cards action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "throwCards");
        actionMap.put("throwCards", throwCardsAction);

        // Bind the F key to the call liar action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "callLiar");
        actionMap.put("callLiar", callLiarAction);

        this.throwCardsButton.addActionListener(throwCardsAction);
        this.callLiarButton.addActionListener(callLiarAction);
    }

    public void setLivesCount(final int livesCount) {
        this.livesLabel.setText(getLivesText(livesCount));
    }

    public void setCallLiarEnabled(final boolean isEnabled) {
        this.canCallLiar = isEnabled;
        this.callLiarButton.setEnabled(isEnabled);
    }

    public void setThrowCardsEnabled(final boolean isEnabled) {
        this.canThrowCards = isEnabled;
        this.throwCardsButton.setEnabled(isEnabled);
    }

    public void setActive(final boolean isActive) {
        this.throwCardsButton.setEnabled(isActive && this.canThrowCards);
        this.callLiarButton.setEnabled(isActive && this.canCallLiar);
    }

    private static String getLivesText(final int initialLivesCount) {
        return "Lives: " + initialLivesCount;
    }

    private static Action createAction(final Runnable action) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        };
    }
}