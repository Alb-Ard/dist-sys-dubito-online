package org.abianchi.dubito.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.abianchi.dubito.messages.PlayerOrderMessage;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;

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
        // Wait for all peers to finish connecting to everyone
        try {
            Thread.sleep(2000);
            // As the owner, I decide the player order.
            // I start as first, then I add the other peers in connection order
            final List<PeerId> playerPeers = new ArrayList<>();
            playerPeers.add(network.getLocalPeerId());
            playerPeers.addAll(network.getPeers().keySet());
            network.sendMessage(new PlayerOrderMessage(network.getLocalPeerId(), null, playerPeers));
            return Optional.of(playerPeers);
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}