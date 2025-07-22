package org.albard.dubito.app;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import org.albard.dubito.app.views.MainWindow;

public final class DubitoApp {
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        new DubitoApp();
    }

    private DubitoApp() throws InterruptedException, InvocationTargetException {
        final Semaphore shutdownLock = new Semaphore(0);
        final MainWindow mainWindow = new MainWindow(shutdownLock);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
        SwingUtilities.invokeLater(() -> mainWindow.setVisible(true));
        shutdownLock.acquire();
        SwingUtilities.invokeAndWait(() -> mainWindow.setVisible(false));
    }
}
