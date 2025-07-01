package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameOnlineSessionController;
import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayerImpl;
import org.abianchi.dubito.app.gameSession.views.*;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.network.PeerStarNetwork;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GameApp {
    private final int playerCount;
    private final PeerId localId;
    private final PeerEndPoint bindEndPoint;

    public GameApp(final PeerId id, final PeerEndPoint bindEndPoint, final int playerCount) throws IOException {
        this.localId = id;
        this.bindEndPoint = bindEndPoint;
        this.playerCount = playerCount;
    }

    public void run(final Consumer<GameBoardView> showBoardConsumer, final Semaphore stopLock, final AppStateModel stateModel) {
        try {
            // creiamo la rete, dove poi gli passeremo l'indirizzo di uno dei giocatori
            // della lobby (la rete in automatico si
            // collegherà a tutti gli altri rimasti
            final PeerNetwork network = PeerStarNetwork.createBound(localId, bindEndPoint.getHost(),
                    bindEndPoint.getPort(), new MessengerFactory(MessageSerializer.createJson()));
            if (!this.initializeNetwork(network)) {
                return;
            }
            this.waitForPlayers(network);

            // se sono l'owner, creo la lista dei peers ottenuti per assegnare un ordine ai
            // vari giocatori
            // nel caso degli altri giocatori, aspetto di ricevere la lista ordinata dei
            // giocatori
            final List<PeerId> playerPeers = this.initializePeers(network).get();
            final List<OnlinePlayer> players = playerPeers.stream().map(OnlinePlayerImpl::new)
                    .collect(Collectors.toList());

            // Stabilisco la GameBoardView e il GameSessionController (versione online)
            // Non uso un lock, in quando è la view stessa che si occupa di aggiornarsi in
            // modo corretto
            final GameBoardView[] view = new GameBoardView[1];
            final GameSessionController<OnlinePlayer> controller = new GameOnlineSessionController<>(players, network,
                    0, () -> view[0].refreshBoard());
            view[0] = new GameBoardView(controller, stateModel);

            // Here we wait for all players to setup their controller/view
            Thread.sleep(5000);

            controller.newRound();
            EventQueue.invokeLater(() -> showBoardConsumer.accept(view[0]));
            stopLock.acquire();
            System.out.println("Closing...");
            network.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected int getPlayerCount() {
        return this.playerCount;
    }

    protected abstract boolean initializeNetwork(final PeerNetwork network);

    protected abstract Optional<List<PeerId>> initializePeers(final PeerNetwork network);

    private void waitForPlayers(final PeerNetwork network) throws InterruptedException {
        // Wait for all OTHER peers to connect (the local peer is not in this list)
        while (network.getPeerCount() < this.playerCount - 1) {
            System.out.println("Waiting for players: " + (network.getPeerCount() + 1) + "/" + this.playerCount);
            Thread.sleep(1000);
        }
    }
}