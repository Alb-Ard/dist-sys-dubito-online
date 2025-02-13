package org.albard.dubito.app.lobby;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record LobbyDisplay(@JsonProperty("id") LobbyId id, @JsonProperty("name") String name,
        @JsonProperty("isPasswordProtected") boolean isPasswordProtected,
        @JsonProperty("currentParticipantCount") int currentParticipantCount,
        @JsonProperty("maxParticipantCount") int maxParticipantCount) {
    public static LobbyDisplay fromLobby(final Lobby lobby) {
        return new LobbyDisplay(lobby.getId(), lobby.getInfo().name(), !lobby.getInfo().password().isBlank(),
                lobby.getParticipants().size(), lobby.getMaxParticipantCount());
    }
}
