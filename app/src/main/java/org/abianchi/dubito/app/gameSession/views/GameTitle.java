package org.abianchi.dubito.app.gameSession.views;

import javax.swing.*;
import java.awt.*;

public class GameTitle extends JLabel {

    public GameTitle(String text) {
        this.setFont(new Font("Serif", Font.PLAIN, 50));
        this.setSize(new Dimension(200, 50));
        setText(text);
    }
}
