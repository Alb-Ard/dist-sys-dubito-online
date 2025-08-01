package org.albard.dubito.lobby.messages;

import java.util.Set;

import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class JoinLobbyMessage extends GameMessageBase {
    private final LobbyId lobbyId;
    private final String password;

    @JsonCreator
    public JoinLobbyMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("newLobbyId") final LobbyId lobbyId, @JsonProperty("password") final String password) {
        super(sender, receipients);
        this.lobbyId = lobbyId;
        this.password = password;
    }

    public LobbyId getLobbyId() {
        return this.lobbyId;
    }

    public String getPassword() {
        return this.password;
    }
}
