package org.albard.dubito.app.views;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;

import org.albard.dubito.app.models.AppStateModel;

public final class ConnectingToGameView extends JPanel {
    public ConnectingToGameView(final AppStateModel stateModel) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalGlue());
        this.add(new JLabel("Starting game..."));
        this.add(Box.createVerticalGlue());
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewTypedValue() == AppStateModel.State.CONNECTING_TO_GAME),
                SwingUtilities::invokeLater);
    }
}