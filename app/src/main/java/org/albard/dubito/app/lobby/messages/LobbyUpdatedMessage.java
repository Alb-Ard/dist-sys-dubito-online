package org.albard.dubito.app.lobby.messages;

import java.util.Set;

import org.albard.dubito.app.lobby.Lobby;
import org.albard.dubito.app.messaging.messages.GameMessageBase;
import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LobbyUpdatedMessage extends GameMessageBase {
    private final Lobby lobby;

    @JsonCreator
    public LobbyUpdatedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients, @JsonProperty("lobby") final Lobby lobby) {
        super(sender, receipients);
        this.lobby = lobby;
    }

    public Lobby getLobby() {
        return this.lobby;
    }
}
