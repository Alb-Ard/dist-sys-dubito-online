package org.abianchi.dubito.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

import org.abianchi.dubito.app.gameSession.views.GameBoardView;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;

public final class Program {
    public static void main(final String[] args) throws IOException, InterruptedException {
        final boolean isOwner = findBooleanArg(args, "owner").orElse(false);
        final String[] ownerEndPoint = isOwner ? new String[0]
                : findArgValue(args, "connect-to").map(x -> x.split(":")).get();
        final String[] bindEndPoint = findArgValue(args, "bind-to").map(x -> x.split(":")).get();
        final PeerId localId = findArgValue(args, "id").map(PeerId::new).get();
        final int nPlayers = findArgValue(args, "player-count").map(Integer::parseInt).get();

        final GameApp app = isOwner
                ? new OwnerGameApp(localId, PeerEndPoint.ofValues(bindEndPoint[0], Integer.parseInt(bindEndPoint[1])),
                        nPlayers)
                : new ClientGameApp(localId, PeerEndPoint.ofValues(bindEndPoint[0], Integer.parseInt(bindEndPoint[1])),
                        PeerEndPoint.ofValues(ownerEndPoint[0], Integer.parseInt(ownerEndPoint[1])), nPlayers);

        final Semaphore shutdownLock = new Semaphore(0);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
        // aspetto qui che il programma venga chiuso (quando chiudo il programma, eseguo
        // il thread passato di chiusura di rete)
        app.run(Program::showBoardInWindow, shutdownLock, new AppStateModel());
    }

    // Searches for an argument in the form of "--name" or "--name=<true/false>"
    private static Optional<Boolean> findBooleanArg(final String[] args, final String name) {
        return findArg(args, name)
                .map(arg -> findArgValue(args, name).map(x -> x.toLowerCase() == "true").orElse(true));
    }

    private static Optional<String> findArgValue(final String[] args, final String name) {
        return findArg(args, name).map(x -> x.split("="))
                .flatMap(x -> x.length > 1 ? Optional.of(x[1]) : Optional.empty());
    }

    // Searches for an argument with the given name ("--name")
    private static Optional<String> findArg(final String[] args, final String name) {
        return Arrays.stream(args).filter(el -> el.startsWith("--" + name)).findFirst();
    }

    private static void showBoardInWindow(GameBoardView boardView) {
        final JFrame frame = new JFrame("Dubito Online");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(boardView);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}