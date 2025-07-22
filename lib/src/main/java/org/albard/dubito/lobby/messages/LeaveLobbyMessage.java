package org.albard.dubito.lobby.messages;

import java.util.Set;

import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

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
