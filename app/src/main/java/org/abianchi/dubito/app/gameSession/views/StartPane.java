package org.abianchi.dubito.app.gameSession.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StartPane extends JPanel {

    private List<JButton> menuItems;
    private JButton focusedButton;

    private JButton selectedButton;

    private Map<JButton, Rectangle> menuBounds;

    public StartPane() {
        this.setSize(new Dimension(700, 700));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.menuItems = new ArrayList<>();
        JButton start = new GameButton("Start Game");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton settings = new GameButton("Settings");
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton exit = new GameButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.menuItems.add(start);
        this.menuItems.add(settings);
        this.menuItems.add(exit);
        this.add(Box.createVerticalGlue());
        this.menuItems.forEach(this::add);
        this.add(Box.createVerticalGlue());

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }
}
