package org.abianchi.dubito.app.views;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class GameButton extends JButton {

    private MenuItemPainter painter;

    public GameButton(MenuItemPainter painter, String text) {
        setText(text);
        this.painter = painter;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g.create();
        Dimension dim = painter.getPreferredSize(g2d, getText());
        setSize(Math.max(getWidth(), dim.width), Math.max(getHeight(), dim.height));
        painter.paint(g2d, this, getBounds(), isSelected(), isFocusPainted());
        g2d.dispose();
    }
}
