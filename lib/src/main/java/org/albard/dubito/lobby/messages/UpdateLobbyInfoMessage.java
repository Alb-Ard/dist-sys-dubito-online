package org.albard.dubito.lobby.messages;

import java.util.Set;

import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateLobbyInfoMessage extends GameMessageBase {
    private final LobbyId lobbyId;
    private final LobbyInfo lobbyInfo;

    @JsonCreator
    public UpdateLobbyInfoMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients, @JsonProperty("lobbyId") final LobbyId lobbyId,
            @JsonProperty("lobbyInfo") final LobbyInfo lobbyInfo) {
        super(sender, receipients);
        this.lobbyId = lobbyId;
        this.lobbyInfo = lobbyInfo;
    }

    public LobbyId getLobbyId() {
        return this.lobbyId;
    }

    public LobbyInfo getLobbyInfo() {
        return this.lobbyInfo;
    }
}
