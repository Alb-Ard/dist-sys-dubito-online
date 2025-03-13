package org.albard.dubito.app.game;

import javax.swing.WindowConstants;

import org.albard.dubito.app.game.views.MainWindow;

public final class GameApp {
    public static void main(String[] args) {
        new GameApp();
    }

    private GameApp() {
        final MainWindow mainWindow = new MainWindow();
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> mainWindow.setVisible(false)));
        mainWindow.setVisible(true);
    }
}
