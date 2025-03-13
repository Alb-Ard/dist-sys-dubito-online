package org.abianchi.dubito.app.views;

import javax.swing.*;

import org.albard.dubito.utils.SimpleComponentFactory;

import java.awt.*;

public final class StartPane extends JPanel {
    public StartPane(final Runnable onStart) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton startButton = SimpleComponentFactory.createGameButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton settingsButton = SimpleComponentFactory.createGameButton("Settings");
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        final JButton exitButton = SimpleComponentFactory.createGameButton("Exit");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(Box.createVerticalGlue());
        this.add(startButton);
        this.add(settingsButton);
        this.add(exitButton);
        this.add(Box.createVerticalGlue());

        startButton.addActionListener(e -> onStart.run());
        exitButton.addActionListener(e -> System.exit(0));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }
}
