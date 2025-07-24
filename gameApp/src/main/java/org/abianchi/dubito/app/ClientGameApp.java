package org.abianchi.dubito.app;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.abianchi.dubito.messages.PlayerOrderMessage;
import org.abianchi.dubito.messages.PlayerReadyMessage;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.utils.Locked;
import org.albard.utils.Logger;

public final class ClientGameApp extends GameApp {
    private final PeerEndPoint remoteEndPoint;

    public ClientGameApp(final PeerId id, final PeerEndPoint bindEndPoint, final PeerEndPoint remoteEndPoint,
            final int playerCount, final AppStateModel appStateModel) throws IOException {
        super(id, bindEndPoint, playerCount, appStateModel);
        this.remoteEndPoint = remoteEndPoint;
    }

    @Override
    protected boolean initializeNetwork(final PeerNetwork network) {
        try {
            /*
             * il metodo connectToPeer mi fa ritornare un valore booleano, se inserito
             * dentro il while mi permette di ritentare la connessione molteplici volte
             */
            while (!network.connectToPeer(remoteEndPoint)) {
                Thread.sleep(1000);
            }
            return true;
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected Optional<List<PeerId>> initializePeers(final PeerNetwork network) {
        try {
            return Optional.of(this.waitForPlayerOrder(network));
        } catch (final InterruptedException ex) {
            Logger.logError(this.getLocalId() + ": Could not initialize peers: " + ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    protected void waitForPlayers(final PeerNetwork network) throws InterruptedException {
        // Wait for all OTHER peers to connect (the local peer is not in this list)
        while (network.getPeerCount() < this.getPlayerCount() - 1) {
            Logger.logInfo(this.getLocalId() + ": Waiting for players: " + network.getPeerCount() + "/"
                    + (this.getPlayerCount() - 1));
            Thread.sleep(1000);
        }
        network.sendMessage(new PlayerReadyMessage(this.getLocalId(), null));
    }

    private List<PeerId> waitForPlayerOrder(final PeerNetwork network) throws InterruptedException {
        final Locked<Optional<PlayerOrderMessage>> orderMessages = Locked.of(Optional.empty());
        network.addOnceMessageListener(message -> {
            if (message instanceof final PlayerOrderMessage orderMessage) {
                System.out
                        .println(this.getLocalId() + ": Player order has been received: " + orderMessage.getPlayers());
                orderMessages.exchange(x -> Optional.of(orderMessage));
                return true;
            }
            return false;
        });
        while (orderMessages.getValue().isEmpty()) {
            Logger.logInfo(this.getLocalId() + ": Waiting for player order");
            Thread.sleep(1000);
        }
        return orderMessages.getValue().get().getPlayers();
    }
}