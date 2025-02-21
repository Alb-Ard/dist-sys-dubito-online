package org.abianchi.dubito.app.views;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class StartMenuView {

    /**
     * this is how i've personally developed a way to properly create a starting menu, with start,
     * leaderboards, settings and exit options
     */
    public StartMenuView() {
            final CardLayout cardlayout = new CardLayout();
            JFrame frame = new JFrame("Dubito Online");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            final Container contentPane = frame.getContentPane();
            contentPane.setLayout(cardlayout);
            contentPane.setPreferredSize(new Dimension(600, 400));
            final StartPane startPane = new StartPane();
            final LobbyPane lobbyPane = new LobbyPane();
            contentPane.add(startPane);
            contentPane.add(lobbyPane);
            frame.add(new StartPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
