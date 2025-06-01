package org.abianchi.dubito.app.gameSession.models;

import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;

import java.util.Optional;

public class OnlinePlayerImpl extends PlayerImpl implements OnlinePlayer{

    private final PeerId playerId;
    public OnlinePlayerImpl(PeerId id) {
        this.playerId = id;
    }
    @Override
    public PeerId getOnlineId() {
        return this.playerId;
    }

    @Override
    public Optional<String> getName() {return Optional.of(this.playerId.id());}

    @Override
    public String toString() {
        return "OnlinePlayerImpl [" + super.toString() + " playerId=" + playerId + "]";
    }
}
