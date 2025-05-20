package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameOnlineSessionController;
import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayerImpl;
import org.abianchi.dubito.app.gameSession.views.*;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.network.PeerStarNetwork;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class App {

    private static void waitForPlayers(PeerNetwork network) throws InterruptedException {
        // Wait for all OTHER peers to connect (the local peer is not in this list)
        while (network.getPeerCount() < 3) {
            System.out.println("Waiting for players: " + (network.getPeerCount() + 1) + "/4");
            Thread.sleep(1000);
        }
    }

    private static void connectWithRetries(PeerNetwork network, String ip, int port) throws InterruptedException {
        /*
         * il metodo connectToPeer mi fa ritornare un valore booleano, se inserito
         * dentro
         * il while mi permette di ritentare la connessione molteplici volte
         */
        while (!network.connectToPeer(PeerEndPoint.ofValues(ip, port))) {
            Thread.sleep(1000);
        }
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        // devo sapere l'owner, lo prendo dagli args
        boolean isOwner = Arrays.stream(args).anyMatch(el -> el.startsWith("--owner"));
        String[] ownerEndPoint = isOwner ? new String[0]
                : Arrays.stream(args).filter(el -> el.startsWith("--connect-to")).findFirst()
                        .map(el -> el.split("=")[1].split(":")).get();
        String[] bindEndPoint = Arrays.stream(args).filter(el -> el.startsWith("--bind-to")).findFirst()
                .map(el -> el.split("=")[1].split(":")).get();
        PeerId localId = Arrays.stream(args).filter(el -> el.startsWith("--id")).findFirst().map(el -> el.split("=")[1])
                .map(PeerId::new).get(); // passo localId quando creo la network
        // creiamo la rete, dove poi gli passeremo l'indirizzo di uno dei giocatori
        // della lobby (la rete in automatico si
        // collegherà a tutti gli altri rimasti
        final PeerNetwork network = PeerStarNetwork.createBound(localId, bindEndPoint[0],
                Integer.parseInt(bindEndPoint[1]),
                new MessengerFactory(MessageSerializer.createJson()));
        if (!isOwner) {
            connectWithRetries(network, ownerEndPoint[0], Integer.parseInt(ownerEndPoint[1]));
        }
        waitForPlayers(network);

        // Create remote players list, then add the local player as first
        final List<OnlinePlayer> players = network.getPeers().keySet().stream()
                .map(OnlinePlayerImpl::new).collect(Collectors.toList());
        players.add(0, new OnlinePlayerImpl(network.getLocalPeerId()));

        // stabilisco la GameBoardView e il GameSessionController (versione online)
        final GameBoardView[] view = new GameBoardView[1];
        final GameSessionController<OnlinePlayer> controller = new GameOnlineSessionController<>(players, network,
                isOwner,
                new Runnable() {
                    @Override
                    public void run() {
                        // qui svolgo la refresh della view
                        view[0].refreshBoard();
                    }
                });
        view[0] = new GameBoardView(controller);

        // Here we wait for all players to setup their controller/view
        Thread.sleep(5000);

        controller.newRound();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                view[0].setBoardVisible(true);
            }
        });
        final Semaphore shutdownLock = new Semaphore(0);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
        // aspetto qui che il programma venga chiuso (quando chiudo il programma, eseguo
        // il thread passato di chiusura di rete)
        shutdownLock.acquire();
        System.out.println("Closing...");
        network.close();
    }
}
