package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameOnlineSessionController;
import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayerImpl;
import org.abianchi.dubito.app.gameSession.views.*;
import org.abianchi.dubito.messages.PlayerOrderMessage;
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
import java.util.Optional;
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
         * dentro il while mi permette di ritentare la connessione molteplici volte
         */
        while (!network.connectToPeer(PeerEndPoint.ofValues(ip, port))) {
            Thread.sleep(1000);
        }
    }

    private static List<PeerId> waitForPlayerOrder(PeerNetwork network) throws InterruptedException {
        final PlayerOrderMessage[] orderMessages = new PlayerOrderMessage[1];
        network.addOnceMessageListener(message -> {
            if (message instanceof PlayerOrderMessage orderMessage) {
                System.out.println("player order has been received: " + orderMessage.getPlayers());
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
        // devo sapere l'owner, lo prendo dagli args
        final boolean isOwner = findBooleanArg(args, "owner").orElse(false);
        final String[] ownerEndPoint = isOwner ? new String[0]
                : findArgValue(args, "connect-to").map(x -> x.split(":")).get();
        final String[] bindEndPoint = findArgValue(args, "bind-to").map(x -> x.split(":")).get();
        final PeerId localId = findArgValue(args, "id").map(PeerId::new).get();
        final int nPlayers = findArgValue(args, "player-count").map(Integer::parseInt).get();

        // creiamo la rete, dove poi gli passeremo l'indirizzo di uno dei giocatori
        // della lobby (la rete in automatico si
        // collegherà a tutti gli altri rimasti
        final PeerNetwork network = PeerStarNetwork.createBound(localId, bindEndPoint[0],
                Integer.parseInt(bindEndPoint[1]), new MessengerFactory(MessageSerializer.createJson()));
        if (!isOwner) {
            connectWithRetries(network, ownerEndPoint[0], Integer.parseInt(ownerEndPoint[1]));
        }
        waitForPlayers(network, nPlayers);

        // se sono l'owner, creo la lista dei peers ottenuti per assegnare un ordine ai
        // vari giocatori
        // nel caso degli altri giocatori, aspetto di ricevere la lista ordinata dei
        // giocatori
        final List<PeerId> playerPeers = new ArrayList<>();
        if (isOwner) {
            // Wait for all peers to finish connecting to everyone
            Thread.sleep(2000);
            // As the owner, I decide the player order.
            // I start as first, then I add the other peers in connection order
            playerPeers.add(network.getLocalPeerId());
            playerPeers.addAll(network.getPeers().keySet());
            network.sendMessage(new PlayerOrderMessage(network.getLocalPeerId(), null, playerPeers));
        } else {
            playerPeers.addAll(waitForPlayerOrder(network));
        }
        final List<OnlinePlayer> players = playerPeers.stream().map(OnlinePlayerImpl::new).collect(Collectors.toList());

        // Stabilisco la GameBoardView e il GameSessionController (versione online)
        // Non uso un lock, in quando è la view stessa che si occupa di aggiornarsi in
        // modo corretto
        final GameBoardView[] view = new GameBoardView[1];
        final GameSessionController<OnlinePlayer> controller = new GameOnlineSessionController<>(players, network,
                isOwner, () -> view[0].refreshBoard());
        view[0] = new GameBoardView(controller, network.getLocalPeerId().id());

        // Here we wait for all players to setup their controller/view
        Thread.sleep(5000);

        controller.newRound();
        EventQueue.invokeLater(() -> view[0].setBoardVisible(true));
        final Semaphore shutdownLock = new Semaphore(0);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLock::release));
        // aspetto qui che il programma venga chiuso (quando chiudo il programma, eseguo
        // il thread passato di chiusura di rete)
        shutdownLock.acquire();
        System.out.println("Closing...");
        network.close();
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
}
