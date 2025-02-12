package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.lobby.LobbyId;
import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LobbyCreatedMessage extends GameMessageBase {
    private final LobbyId newLobbyId;

    @JsonCreator
    public LobbyCreatedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("newLobbyId") final LobbyId newLobbyId) {
        super(sender, receipients);
        this.newLobbyId = newLobbyId;
    }

    public LobbyId getNewLobbyId() {
        return this.newLobbyId;
    }
}
