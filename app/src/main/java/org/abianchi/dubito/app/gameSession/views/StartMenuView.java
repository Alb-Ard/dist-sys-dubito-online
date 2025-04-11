package org.abianchi.dubito.app.gameSession.views;

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
            final BorderLayout borderLayout = new BorderLayout();
            JFrame frame = new JFrame("Dubito Online");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            final Container contentPane = frame.getContentPane();
            contentPane.setLayout(borderLayout);
            contentPane.setPreferredSize(new Dimension(700, 500));
            final StartPane startPane = new StartPane();
            contentPane.add(startPane, BorderLayout.CENTER);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
