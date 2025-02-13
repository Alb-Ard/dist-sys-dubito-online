package org.albard.dubito.app.lobby.messages;

import java.util.Set;

import org.albard.dubito.app.lobby.models.LobbyId;
import org.albard.dubito.app.messaging.messages.GameMessageBase;
import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LeaveLobbyMessage extends GameMessageBase {
    private final LobbyId lobbyId;

    @JsonCreator
    public LeaveLobbyMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("newLobbyId") final LobbyId lobbyId) {
        super(sender, receipients);
        this.lobbyId = lobbyId;
    }

    public LobbyId getLobbyId() {
        return this.lobbyId;
    }
}
