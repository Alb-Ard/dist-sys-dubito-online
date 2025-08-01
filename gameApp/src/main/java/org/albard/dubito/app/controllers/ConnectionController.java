package org.albard.dubito.app.controllers;

import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.AppStateModel.State;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.serialization.MessageSerializer;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.utils.Logger;

public final class ConnectionController {
    private final AppStateModel stateModel;

    public ConnectionController(final AppStateModel stateModel) {
        this.stateModel = stateModel;
    }

    public void connectTo(final PeerEndPoint endPoint) {
        try {
            final PeerNetwork network = PeerNetwork.createBound(PeerId.createNew(), "0.0.0.0", 0,
                    new MessengerFactory(MessageSerializer.createJson()));
            if (!network.connectToPeer(endPoint)) {
                // TODO: Show error to user
                return;
            }
            this.stateModel.setLobbyNetwork(network);
            this.stateModel.setState(State.IN_LOBBY_LIST);
        } catch (final Exception ex) {
            Logger.logError("Could not connect to " + endPoint + ": " + ex.getMessage());
        }
    }
}
