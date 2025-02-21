package org.abianchi.dubito.app.views;

import javax.swing.*;
import java.awt.*;

interface MenuItemPainter {

    public void paint(Graphics2D g2d, JButton button, Rectangle bounds, boolean isSelected, boolean isFocused);

    public Dimension getPreferredSize(Graphics2D g2d, String text);

}
