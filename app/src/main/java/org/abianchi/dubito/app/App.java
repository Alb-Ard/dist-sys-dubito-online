package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.views.*;

import java.awt.*;

public class App {
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StartMenuView();
            }
        });

    }
}
