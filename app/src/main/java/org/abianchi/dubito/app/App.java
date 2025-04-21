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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class App {

    public static void main(final String[] args) throws IOException, InterruptedException {
        // devo sapere l'owner, lo prendo dagli args
        boolean isOwner = Arrays.stream(args).anyMatch(el -> el.startsWith("--owner"));
        String[] ownerEndPoint = isOwner ? new String[0] : Arrays.stream(args).filter(el -> el.startsWith("--connect-to")).findFirst()
                .map(el -> el.split("=")[1].split(":")).get();
        PeerId localId = Arrays.stream(args).filter(el -> el.startsWith("--id")).findFirst().map(el -> el.split("=")[1])
                .map(PeerId::new).get(); // passo localId quando creo la network
        // creiamo la rete, dove poi gli passeremo l'indirizzo di uno dei giocatori della lobby (la rete in automatico si
        // collegherà a tutti gli altri rimasti
        final PeerNetwork network = PeerStarNetwork.createBound(localId, "0.0.0.0",isOwner ? 9000 : 0,
                new MessengerFactory(MessageSerializer.createJson()));
        if (!isOwner && !network.connectToPeer(PeerEndPoint.createFromValues(ownerEndPoint[0],Integer.parseInt(ownerEndPoint[1])))) {
            return;
        }
        Thread.sleep(3000); // breve pausa per far collegare tutti i players
        final List<OnlinePlayer> players = network.getPeers().keySet().stream()
                .map(OnlinePlayerImpl::new).collect(Collectors.toList());

        // stabilisco la GameBoardView e il GameSessionController (versione online)
        final GameBoardView[] view = new GameBoardView[1];
        final GameSessionController controller = new GameOnlineSessionController(players, network, isOwner, new Runnable() {
            @Override
            public void run() {
                //qui svolgo la refresh della view
                view[0].refreshBoard();
            }
        });
        view[0] = new GameBoardView(controller);
        controller.newRound();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                view[0].setBoardVisible(true);
            }
        });
        final Semaphore shutdownLock = new Semaphore(0);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
        // aspetto qui che il programma venga chiuso (quando chiudo il programma, eseguo il thread passato di chiusura di rete)
        shutdownLock.acquire();
        System.out.println("Closing...");
        network.close();
    }
}
