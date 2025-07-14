package org.abianchi.dubito.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.abianchi.dubito.messages.PlayerOrderMessage;
import org.abianchi.dubito.messages.PlayerReadyMessage;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.utils.Logger;

public final class OwnerGameApp extends GameApp {
    public OwnerGameApp(final PeerId id, final PeerEndPoint bindEndPoint, final int playerCount,
            final AppStateModel appStateModel) throws IOException {
        super(id, bindEndPoint, playerCount, appStateModel);
    }

    @Override
    protected boolean initializeNetwork(final PeerNetwork network) {
        return true;
    }

    @Override
    protected Optional<List<PeerId>> initializePeers(PeerNetwork network) {
        // As the owner, I decide the player order.
        // I start as first, then I add the other peers in connection order
        final List<PeerId> playerPeers = new ArrayList<>();
        playerPeers.add(network.getLocalPeerId());
        playerPeers.addAll(network.getPeers().keySet());
        network.sendMessage(new PlayerOrderMessage(network.getLocalPeerId(), null, playerPeers));
        return Optional.of(playerPeers);
    }

    @Override
    protected void waitForPlayers(PeerNetwork network) throws InterruptedException {
        // Wait for all OTHER peers to connect and send the PlayerReadyMessage (the
        // local peer is not in this list)
        int[] readyPlayerCount = new int[] { 0 };
        final MessageHandler playerReadyHandler = message -> {
            if (message instanceof PlayerReadyMessage) {
                readyPlayerCount[0]++;
                return true;
            }
            return false;
        };
        network.addMessageListener(playerReadyHandler);
        while (readyPlayerCount[0] < this.getPlayerCount() - 1) {
            Logger.logInfo(this.getLocalId() + ": Waiting for READY players: " + readyPlayerCount[0] + "/"
                    + (this.getPlayerCount() - 1));
            Thread.sleep(1000);
        }
        network.queueRemoveMessageListener(playerReadyHandler);
    }
}