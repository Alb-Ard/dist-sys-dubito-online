package org.abianchi.dubito.app.views;

import javax.swing.*;
import java.awt.*;

public class GameButton extends JButton {

    public GameButton(String text) {
        this.setSize(new Dimension(200, 50));
        setText(text);
        setBackground(Color.black);
        setForeground(Color.white);
    }
}
