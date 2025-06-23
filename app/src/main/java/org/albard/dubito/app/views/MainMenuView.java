package org.albard.dubito.app.views;

import java.awt.*;

import javax.swing.*;

import org.abianchi.dubito.app.gameSession.views.GameButton;
import org.abianchi.dubito.app.gameSession.views.GameTitle;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.AppStateModel.State;

public final class MainMenuView extends JPanel {
    public MainMenuView(final AppStateModel stateModel) {
        this.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel titlePanel = new JPanel();

        final JButton start = new GameButton("Start Game");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton exit = new GameButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JLabel titleLabel = new GameTitle("Dubito Online!");

        buttonPanel.add(start);
        buttonPanel.add(exit);
        titlePanel.add(titleLabel);

        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(titlePanel, BorderLayout.NORTH);


        exit.addActionListener(e -> System.exit(0));
        start.addActionListener(e -> stateModel.setState(State.IN_LOBBY_SERVER_CONNECTION));
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewTypedValue() == AppStateModel.State.IN_MAIN_MENU));
    }
}
