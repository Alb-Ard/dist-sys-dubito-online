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
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
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
        });
    }

    public class StartPane extends JPanel {

        private List<JButton> menuItems;
        private JButton focusedButton;

        private JButton selectedButton;

        private MenuItemPainter painter;
        private Map<JButton, Rectangle> menuBounds;

        public StartPane() {
            setBackground(Color.BLACK);
            painter = new SimpleMenuItemPainter();
            menuItems = new ArrayList<>(5);
            JButton start = new JButton("Start Game");
            JButton leaderboards = new JButton("LeaderBoards");
            JButton settings = new JButton("Settings");
            JButton exit = new JButton("Exit");
            menuItems.add(start);
            menuItems.add(leaderboards);
            menuItems.add(settings);
            menuItems.add(exit);

            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            MouseAdapter ma = new MouseAdapter() {

                @Override
                public void mouseMoved(MouseEvent e) {
                    focusedButton = null;
                    for (JButton button : menuItems) {
                        Rectangle bounds = menuBounds.get(button);
                        if (bounds.contains(e.getPoint())) {
                            focusedButton = button;
                            repaint();
                            break;
                        }
                    }
                }

            };

            addMouseListener(ma);
            addMouseMotionListener(ma);

            InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "arrowDown");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "arrowUp");

            am.put("arrowDown", new MenuAction(1));
            am.put("arrowUp", new MenuAction(-1));

        }

        @Override
        public void invalidate() {
            menuBounds = null;
            super.invalidate();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (menuBounds == null) {
                menuBounds = new HashMap<>(menuItems.size());
                int width = 0;
                int height = 0;
                for (JButton button : menuItems) {
                    Dimension dim = painter.getPreferredSize(g2d, button.getText());
                    width = Math.max(width, dim.width);
                    height = Math.max(height, dim.height);
                }

                int x = (getWidth() - (width + 10)) / 2;

                int totalHeight = (height + 10) * menuItems.size();
                totalHeight += 5 * (menuItems.size() - 1);

                int y = (getHeight() - totalHeight) / 2;

                for (JButton button : menuItems) {
                    menuBounds.put(button, new Rectangle(x, y, width + 10, height + 10));
                    y += height + 10 + 5;
                }

            }
            for (JButton button : menuItems) {
                Rectangle bounds = menuBounds.get(button);
                boolean isSelected = button.equals(selectedButton);
                boolean isFocused = button.equals(focusedButton);
                painter.paint(g2d, button, bounds, isSelected, isFocused);
            }
            g2d.dispose();
        }

        public class MenuAction extends AbstractAction {

            private final int delta;

            public MenuAction(int delta) {
                this.delta = delta;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = menuItems.indexOf(selectedButton);
                if (index < 0) {
                    selectedButton = menuItems.get(0);
                }
                index += delta;
                if (index < 0) {
                    selectedButton = menuItems.get(menuItems.size() - 1);
                } else if (index >= menuItems.size()) {
                    selectedButton = menuItems.get(0);
                } else {
                    selectedButton = menuItems.get(index);
                }
                repaint();
            }

        }

    }

    public class LobbyPane extends JPanel {

    }

    public interface MenuItemPainter {

        public void paint(Graphics2D g2d, JButton button, Rectangle bounds, boolean isSelected, boolean isFocused);

        public Dimension getPreferredSize(Graphics2D g2d, String text);

    }

    public class SimpleMenuItemPainter implements MenuItemPainter {

        public Dimension getPreferredSize(Graphics2D g2d, String text) {
            return g2d.getFontMetrics().getStringBounds(text, g2d).getBounds().getSize();
        }

        @Override
        public void paint(Graphics2D g2d, JButton button, Rectangle bounds, boolean isSelected, boolean isFocused) {
            FontMetrics fm = g2d.getFontMetrics();
            if (isSelected) {
                paintBackground(g2d, bounds, Color.BLUE, Color.WHITE);
            } else if (isFocused) {
                paintBackground(g2d, bounds, Color.BLUE, Color.BLACK);
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

}
