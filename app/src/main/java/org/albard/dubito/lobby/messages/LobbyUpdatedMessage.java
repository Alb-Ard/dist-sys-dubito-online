package org.albard.dubito.lobby.messages;

import java.util.Set;

import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

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
