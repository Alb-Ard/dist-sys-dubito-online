package org.albard.dubito.lobby.messages;

import java.util.Set;

import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateLobbyMessage extends GameMessageBase {
    private final LobbyInfo lobbyInfo;

    @JsonCreator
    public CreateLobbyMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("lobbyInfo") final LobbyInfo lobbyInfo) {
        super(sender, receipients);
        this.lobbyInfo = lobbyInfo;
    }

    public LobbyInfo getLobbyInfo() {
        return this.lobbyInfo;
    }
}
