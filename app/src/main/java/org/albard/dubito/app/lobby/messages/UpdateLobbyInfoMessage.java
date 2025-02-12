package org.albard.dubito.app.lobby.messages;

import java.util.Set;

import org.albard.dubito.app.lobby.LobbyId;
import org.albard.dubito.app.lobby.LobbyInfo;
import org.albard.dubito.app.messaging.messages.GameMessageBase;
import org.albard.dubito.app.network.PeerId;

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
