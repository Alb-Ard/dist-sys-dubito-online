package org.abianchi.dubito.app.views;

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

    private MenuItemPainter painter;

    private Map<JButton, Rectangle> menuBounds;

    public StartPane() {
        setBackground(Color.BLACK);
        this.painter = new SimpleMenuItemPainter();
        this.menuItems = new ArrayList<>();
        JButton start = new GameButton(painter,  "Start Game");
        JButton leaderboards = new GameButton(painter, "LeaderBoards");
        JButton settings = new GameButton(painter, "Settings");
        JButton exit = new GameButton(painter, "Exit");
        this.menuItems.add(start);
        this.menuItems.add(leaderboards);
        this.menuItems.add(settings);
        this.menuItems.add(exit);

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
                    Rectangle bounds = button.getBounds();
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

        am.put("arrowDown", new MenuAction(-1));
        am.put("arrowUp", new MenuAction(1));

    }

    /*
        serve per effettuare modifiche alla view (invalidando il suo layout e quello dei padri, ridisegnando con i nuovi layout
     */
    @Override
    public void invalidate() {
        menuBounds = null;
        super.invalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }


    private class MenuAction extends AbstractAction {

        private int delta;

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
