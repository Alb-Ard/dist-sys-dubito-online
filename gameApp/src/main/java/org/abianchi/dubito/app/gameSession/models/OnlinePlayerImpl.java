package org.abianchi.dubito.app.gameSession.models;

import org.albard.dubito.network.PeerId;

import java.util.Optional;

public class OnlinePlayerImpl extends PlayerImpl implements OnlinePlayer {
    private final PeerId playerId;
    private final String username;

    public OnlinePlayerImpl(final PeerId id, final String username) {
        this.playerId = id;
        this.username = username;
    }

    @Override
    public PeerId getOnlineId() {
        return this.playerId;
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(this.username);
    }

    @Override
    public String toString() {
        return "OnlinePlayerImpl [" + super.toString() + " playerId=" + playerId + "]";
    }
}
