package org.albard.dubito.app.lobby;

import java.util.HashSet;
import java.util.Set;

import org.albard.dubito.app.network.PeerId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Lobby {
    private final LobbyId id;

    private final PeerId owner;
    private final LobbyInfo info;
    private final Set<PeerId> participants;
    private final int maxParticipantCount = 4;

    @JsonCreator
    private Lobby(@JsonProperty("id") final LobbyId id, @JsonProperty("owner") final PeerId owner,
            @JsonProperty("info") final LobbyInfo info, @JsonProperty("participants") final Set<PeerId> participants) {
        this.id = id;
        this.owner = owner;
        this.info = info;
        this.participants = participants;
    }

    public Lobby(final LobbyId id, final PeerId owner, final LobbyInfo info) {
        this(id, owner, info, new HashSet<>());
        this.participants.add(owner);
    }

    public LobbyId getId() {
        return this.id;
    }

    public PeerId getOwner() {
        return this.owner;
    }

    public LobbyInfo getInfo() {
        return this.info;
    }

    public Set<PeerId> getParticipants() {
        return Set.copyOf(this.participants);
    }

    public int getMaxParticipantCount() {
        return this.maxParticipantCount;
    }

    public Lobby setInfo(final LobbyInfo newInfo) {
        return new Lobby(this.id, this.owner, newInfo, this.participants);
    }

    public Lobby addParticipant(final PeerId newParticipant) {
        if (this.participants.size() >= this.maxParticipantCount) {
            return this;
        }
        final Set<PeerId> newParticipants = new HashSet<>(this.participants);
        newParticipants.add(newParticipant);
        return new Lobby(this.id, this.owner, this.info, newParticipants);
    }

    public Lobby removeParticipant(final PeerId participant) {
        final Set<PeerId> newParticipants = new HashSet<>(this.participants);
        newParticipants.remove(participant);
        return new Lobby(this.id, this.owner, this.info, newParticipants);
    }
}
