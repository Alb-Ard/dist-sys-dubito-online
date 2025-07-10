package org.albard.dubito.app;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.albard.dubito.lobby.app.server.LobbyServerHost;
import org.albard.utils.Logger;

public final class DubitoAppTest {
    @FunctionalInterface
    private interface AppLauncher {
        void run(final String[] args) throws Exception;
    }

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(10);

    public static void main(final String[] args) throws InterruptedException {
        waitAny(runApp(DubitoApp::main, args), runApp(DubitoApp::main, args), runApp(LobbyServerHost::main, args));
    }

    private static void waitAny(final Future<?>... tasks) throws InterruptedException {
        while (Arrays.stream(tasks).anyMatch(x -> x.isDone())) {
            Thread.sleep(50);
        }
    }

    private static Future<?> runApp(final AppLauncher main, final String[] args) {
        return appExecutor.submit(() -> {
            try {
                main.run(args);
            } catch (final Exception ex) {
                Logger.logError("UNHANDLED EXCEPTION: " + ex.getMessage());
            }
        });
    }
}
