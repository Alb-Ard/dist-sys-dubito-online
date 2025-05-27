package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameOnlineSessionController;
import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayerImpl;
import org.abianchi.dubito.app.gameSession.models.Player;
import org.abianchi.dubito.app.gameSession.models.PlayerImpl;
import org.abianchi.dubito.app.gameSession.views.*;
import org.abianchi.dubito.messages.PlayerOrderMessage;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.network.PeerStarNetwork;
import org.albard.dubito.utils.Locked;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class App {

    private static void waitForPlayers(PeerNetwork network, int nPlayers) throws InterruptedException {
        // Wait for all OTHER peers to connect (the local peer is not in this list)
        while (network.getPeerCount() < nPlayers - 1) {
            System.out.println("Waiting for players: " + (network.getPeerCount() + 1) + "/" + nPlayers);
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

    private static List<PeerId> waitForPlayerOrder(PeerNetwork network) throws InterruptedException {
        final PlayerOrderMessage[] orderMessages = new PlayerOrderMessage[1];
        network.addOnceMessageListener(message -> {
            if(message instanceof PlayerOrderMessage orderMessage) {
                System.out.println("player order has been received: " + orderMessage.getPlayers() );
                orderMessages[0] = orderMessage;
                return true;
            }
            return false;
        });
        while (orderMessages[0] == null) {
            System.out.println("Waiting for player order");
            Thread.sleep(1000);
        }
        return orderMessages[0].getPlayers();
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        //offline version
        /*
        final List<Player> players = List.of(new PlayerImpl(), new PlayerImpl());
        final Locked<GameBoardView> view = Locked.of(null);
        final GameSessionController<Player> controller = new GameSessionController<>(players);
        controller.newRound();
        view.exchange(v -> new GameBoardView(controller, "Dubito"));

        // Here we wait for all players to setup their controller/view
        Thread.sleep(5000);


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.exchange(v -> {
                    v.setBoardVisible(true);
                    return v;
                });
            }
        });
        final Semaphore shutdownLock = new Semaphore(0);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
        // aspetto qui che il programma venga chiuso (quando chiudo il programma, eseguo
        // il thread passato di chiusura di rete)
        shutdownLock.acquire();
        System.out.println("Closing...");


        return;
        */
        //online version
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
        int nPlayers = Integer.parseInt(Arrays.stream(args).filter(el -> el.startsWith("--player-count")).findFirst()
                .map(el -> el.split("=")[1]).get());
        waitForPlayers(network, nPlayers);

        // se sono l'owner, creo la lista dei peers ottenuti per assegnare un ordine ai vari giocatori
        // nel caso degli altri giocatori, aspetto di ricevere la lista ordinata dei giocatori
        final List<PeerId> playerPeers = new ArrayList<>();
        if (isOwner) {
            playerPeers.add(network.getLocalPeerId());
            playerPeers.addAll(network.getPeers().keySet());
            Thread.sleep(2000); // per evitare che il messaggio venga perso
            network.sendMessage(new PlayerOrderMessage(network.getLocalPeerId(), null, playerPeers));
        } else {
            playerPeers.addAll(waitForPlayerOrder(network));
        }

        // Create remote players list, then add the local player as first
        final List<OnlinePlayer> players = playerPeers.stream()
                .map(OnlinePlayerImpl::new).collect(Collectors.toList());

        // stabilisco la GameBoardView e il GameSessionController (versione online)
        // lo racchiudo in un Locked per evitare un costante refresh della board fatto da più peers
        // sto essenzialmente cercando di dare un ordine ai refresh della board
        final Locked<GameBoardView> view = Locked.of(null);
        final GameSessionController<OnlinePlayer> controller = new GameOnlineSessionController<>(players, network,
                isOwner,() -> {
                    view.exchange(v -> {
                        v.refreshBoard();
                        return v;
                    });
                });
        view.exchange(v -> new GameBoardView(controller, network.getLocalPeerId().id()));

        // Here we wait for all players to setup their controller/view
        Thread.sleep(5000);

        controller.newRound();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.exchange(v -> {
                            v.setBoardVisible(true);
                            return v;
                        });
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
