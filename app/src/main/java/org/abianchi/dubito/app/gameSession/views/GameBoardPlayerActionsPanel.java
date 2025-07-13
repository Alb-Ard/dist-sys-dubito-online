package org.abianchi.dubito.app.gameSession.views;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
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

    public GameBoardPlayerActionsPanel(final int initialLivesCount, final boolean vertical, final String playerName,
            final Runnable playCardsListener, final Runnable callLiarListener, final boolean callLiarEnabler) {
        /** buttons and label for player's lives */
        if (vertical) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        this.livesLabel = new JLabel(getLivesText(initialLivesCount));
        this.playerNameLabel = new JLabel(playerName.length() > 12 ? playerName.substring(0, 12) + "..." : playerName);
        GameButton throwCardsButton = new GameButton("Throw Cards (T)");
        GameButton callLiarButton = new GameButton("Call Liar (F)");
        this.add(livesLabel);
        this.add(playerNameLabel);
        this.add(throwCardsButton);
        this.add(callLiarButton);

        // Add an action map/input map to the content pane
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // Create actions for throw cards and call liar
        Action throwCardsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playCardsListener.run();
            }
        };

        Action callLiarAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                callLiarListener.run();
            }
        };

        // Bind the T key to the throw cards action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "throwCards");
        actionMap.put("throwCards", throwCardsAction);

        // Bind the F key to the call liar action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "callLiar");
        actionMap.put("callLiar", callLiarAction);

        throwCardsButton.addActionListener(e -> playCardsListener.run());
        callLiarButton.addActionListener(e -> callLiarListener.run());
    }

    public void setLivesCount(final int livesCount) {
        this.livesLabel.setText(getLivesText(livesCount));
    }

    public void setActive(final boolean isActive) {
        for (final Component component : this.getComponents()) {
            if (component instanceof AbstractButton) {
                component.setEnabled(isActive);
            } else if (component instanceof JPanel subPanel) {
                // For button panels
                for (final Component subComponent : subPanel.getComponents()) {
                    subComponent.setEnabled(isActive);
                }
            }
        }
    }

    private static String getLivesText(final int initialLivesCount) {
        return "Lives: " + initialLivesCount;
    }

}