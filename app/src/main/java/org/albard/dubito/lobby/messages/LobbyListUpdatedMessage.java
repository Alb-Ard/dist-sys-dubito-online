package org.albard.dubito.lobby.messages;

import java.util.List;
import java.util.Set;

import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LobbyListUpdatedMessage extends GameMessageBase {
    private final List<LobbyDisplay> lobbies;

    @JsonCreator
    public LobbyListUpdatedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients,
            @JsonProperty("lobbies") final List<LobbyDisplay> lobbies) {
        super(sender, receipients);
        this.lobbies = lobbies;
    }

    public List<LobbyDisplay> getLobbies() {
        return List.copyOf(this.lobbies);
    }
}
