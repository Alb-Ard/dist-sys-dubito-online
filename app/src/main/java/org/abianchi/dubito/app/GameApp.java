package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameOnlineSessionController;
import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayerImpl;
import org.abianchi.dubito.app.gameSession.views.GameBoardView;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.serialization.MessageSerializer;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.network.PeerStarNetwork;
import org.albard.utils.ListenerUtils;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

public abstract class GameApp {
    private final int playerCount;
    private final PeerId localId;
    private final PeerEndPoint bindEndPoint;
    private final Set<Runnable> gameStartedListeners = Collections.synchronizedSet(new HashSet<>());
    private final AppStateModel appStateModel;

    public GameApp(final PeerId id, final PeerEndPoint bindEndPoint, final int playerCount,
            final AppStateModel appStateModel) throws IOException {
        this.localId = id;
        this.bindEndPoint = bindEndPoint;
        this.playerCount = playerCount;
        this.appStateModel = appStateModel;
    }

    public void run(final Consumer<GameBoardView> showBoardConsumer, final Semaphore stopLock) {
        try {
            // creiamo la rete, dove poi gli passeremo l'indirizzo di uno dei giocatori
            // della lobby (la rete in automatico si
            // collegherà a tutti gli altri rimasti
            System.out.println(localId + ": Binding to " + bindEndPoint);
            final PeerNetwork network = PeerStarNetwork.createBound(localId, bindEndPoint.getHost(),
                    bindEndPoint.getPort(), new MessengerFactory(MessageSerializer.createJson()));
            System.out.println(localId + ": Initializing network");
            if (!this.initializeNetwork(network)) {
                return;
            }
            System.out.println(localId + ": Waiting for players");
            this.waitForPlayers(network);
            System.out.println(localId + ": All players connected");

            // se sono l'owner, creo la lista dei peers ottenuti per assegnare un ordine ai
            // vari giocatori
            // nel caso degli altri giocatori, aspetto di ricevere la lista ordinata dei
            // giocatori
            final List<PeerId> playerPeers = this.initializePeers(network).get();
            final List<OnlinePlayer> players = playerPeers.stream()
                    .map(x -> new OnlinePlayerImpl(x, this.getPlayerUsername(x, playerPeers.indexOf(x))))
                    .collect(Collectors.toList());

            // Stabilisco la GameBoardView e il GameSessionController (versione online)
            // Non uso un lock, in quando è la view stessa che si occupa di aggiornarsi in
            // modo corretto
            final GameBoardView[] view = new GameBoardView[1];
            final GameSessionController<OnlinePlayer> controller = new GameOnlineSessionController<>(players, network,
                    0, () -> SwingUtilities.invokeLater(view[0]::refreshBoard));
            view[0] = new GameBoardView(controller);

            // Here we wait for all players to setup their controller/view
            Thread.sleep(1000);

            ListenerUtils.runAll(this.gameStartedListeners);

            controller.newRound();
            EventQueue.invokeLater(() -> showBoardConsumer.accept(view[0]));
            stopLock.acquire();
            System.out.println(localId + ": Closing...");
            network.close();
        } catch (final Exception ex) {
            System.err.println(localId + ": Could not run app: " + ex.getMessage());
        }
    }

    public void addGameStartedListener(final Runnable listener) {
        this.gameStartedListeners.add(listener);
    }

    public void removeGameStartedListener(final Runnable listener) {
        this.gameStartedListeners.remove(listener);
    }

    protected int getPlayerCount() {
        return this.playerCount;
    }

    protected PeerId getLocalId() {
        return this.localId;
    }

    protected abstract boolean initializeNetwork(final PeerNetwork network);

    protected abstract Optional<List<PeerId>> initializePeers(final PeerNetwork network);

    protected abstract void waitForPlayers(final PeerNetwork network) throws InterruptedException;

    private String getPlayerUsername(final PeerId peerId, final int playerIndex) {
        return this.appStateModel.getUserClient().flatMap(x -> x.getUser(peerId)).map(x -> x.name())
                .orElse("Player " + playerIndex);
    }
}