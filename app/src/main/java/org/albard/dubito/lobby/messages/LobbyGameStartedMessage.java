package org.albard.dubito.lobby.messages;

import java.util.Set;

import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LobbyGameStartedMessage extends GameMessageBase {
    private final LobbyId lobbyId;
    private final PeerEndPoint ownerEndPoint;

    @JsonCreator
    public LobbyGameStartedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients, @JsonProperty("lobbyId") final LobbyId lobbyId,
            @JsonProperty("ownerEndPoint") final PeerEndPoint ownerEndPoint) {
        super(sender, receipients);
        this.lobbyId = lobbyId;
        this.ownerEndPoint = ownerEndPoint;
    }

    public LobbyId getLobbyId() {
        return this.lobbyId;
    }

    public PeerEndPoint getOwnerEndPoint() {
        return this.ownerEndPoint;
    }
}
