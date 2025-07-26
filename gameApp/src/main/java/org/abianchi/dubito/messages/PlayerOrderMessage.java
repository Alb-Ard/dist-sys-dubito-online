package org.abianchi.dubito.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;

import java.util.List;
import java.util.Set;

public class PlayerOrderMessage extends GameMessageBase {
    private final List<PeerId> players;

    @JsonCreator
    public PlayerOrderMessage(@JsonProperty("sender")final PeerId sender,
                              @JsonProperty("receipients")final Set<PeerId> receipients,
                              @JsonProperty("players") final List<PeerId> players) {
        super(sender, receipients);
        this.players = players;
    }

    public List<PeerId> getPlayers() {
        return this.players;
    }
}
