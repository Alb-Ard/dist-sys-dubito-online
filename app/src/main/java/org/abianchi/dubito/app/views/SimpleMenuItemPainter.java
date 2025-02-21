package org.abianchi.dubito.app.views;

import javax.swing.*;
import java.awt.*;

class SimpleMenuItemPainter implements MenuItemPainter {

    public Dimension getPreferredSize(Graphics2D g2d, String text) {
        return g2d.getFontMetrics().getStringBounds(text, g2d).getBounds().getSize();
    }

    @Override
    public void paint(Graphics2D g2d, JButton button, Rectangle bounds, boolean isSelected, boolean isFocused) {
        FontMetrics fm = g2d.getFontMetrics();
        if (isSelected || isFocused) {
            paintBackground(g2d, bounds, Color.BLUE, Color.WHITE);
        } else {
            paintBackground(g2d, bounds, Color.DARK_GRAY, Color.LIGHT_GRAY);
        }
        int x = bounds.x + ((bounds.width - fm.stringWidth(button.getText())) / 2);
        int y = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.setColor(isSelected ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString(button.getText(), x, y);
    }

    protected void paintBackground(Graphics2D g2d, Rectangle bounds, Color background, Color foreground) {
        g2d.setColor(background);
        g2d.fill(bounds);
        g2d.setColor(foreground);
        g2d.draw(bounds);
    }

}
