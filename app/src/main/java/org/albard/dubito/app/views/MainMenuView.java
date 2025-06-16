package org.albard.dubito.app.views;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.abianchi.dubito.app.gameSession.views.GameButton;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.AppStateModel.State;

public final class MainMenuView extends JPanel {
    public MainMenuView(final AppStateModel stateModel) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton start = new GameButton("Start Game");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton settings = new GameButton("Settings");
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton exit = new GameButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(Box.createVerticalGlue());
        this.add(start);
        this.add(settings);
        this.add(exit);
        this.add(Box.createVerticalGlue());

        exit.addActionListener(e -> System.exit(0));
        start.addActionListener(e -> stateModel.setState(State.IN_LOBBY_SERVER_CONNECTION));
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewTypedValue() == AppStateModel.State.IN_MAIN_MENU));
    }
}
