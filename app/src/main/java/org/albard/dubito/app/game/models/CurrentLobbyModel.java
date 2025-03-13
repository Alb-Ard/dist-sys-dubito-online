package org.albard.dubito.app.game.models;

import java.util.List;
import java.util.function.BiFunction;

import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.network.PeerId;

import com.jgoodies.binding.beans.Model;

public final class CurrentLobbyModel extends Model {
    public static final String LOBBY_NAME_PROPERTY = "lobbyName";
    public static final String LOBBY_PASSWORD_PROPERTY = "lobbyPassword";
    public static final String LOBBY_OWNER_PROPERTY = "lobbyOwnerId";
    public static final String PARTICIPANT_NAMES_PROPERTY = "participantNames";
    public static final String MAX_PARTICIPANT_COUNT_PROPERTY = "maxParticipantCount";

    private final PeerId localPeerId;

    private String lobbyName = "";
    private String lobbyPassword = "";
    private PeerId lobbyOwnerId = new PeerId("");
    private List<String> participantNames = List.of();
    private int maxParticipantCount = 0;

    public CurrentLobbyModel(final PeerId localPeerId) {
        this.localPeerId = localPeerId;
    }

    public PeerId getLocalPeerId() {
        return this.localPeerId;
    }

    public String getLobbyName() {
        return this.lobbyName;
    }

    public String getLobbyPassword() {
        return this.lobbyPassword;
    }

    public PeerId getLobbyOwnerId() {
        return lobbyOwnerId;
    }

    public List<String> getParticipantNames() {
        return List.copyOf(this.participantNames);
    }

    public int getMaxParticipantCount() {
        return this.maxParticipantCount;
    }

    public boolean isLocalPeerOwner() {
        return this.localPeerId.equals(this.lobbyOwnerId);
    }

    public void setFromLobby(final Lobby currentLobby, final BiFunction<Lobby, PeerId, String> participantNameMapper) {
        this.setLobbyName(currentLobby.getInfo().name());
        this.setLobbyPassword(currentLobby.getInfo().password());
        this.setLobbyOwnerId(currentLobby.getOwner());
        this.setParticipantNames(currentLobby.getParticipants().stream()
                .map(x -> participantNameMapper.apply(currentLobby, x)).toList());
        this.setMaxParticipantCount(currentLobby.getMaxParticipantCount());
    }

    public void setLobbyName(final String lobbyName) {
        final String oldName = this.lobbyName;
        this.lobbyName = lobbyName;
        this.firePropertyChange(LOBBY_NAME_PROPERTY, oldName, lobbyName);
    }

    public void setLobbyPassword(final String lobbyPassword) {
        final String oldPassword = this.lobbyPassword;
        this.lobbyPassword = lobbyPassword;
        this.firePropertyChange(LOBBY_PASSWORD_PROPERTY, oldPassword, lobbyPassword);
    }

    public void setLobbyOwnerId(PeerId lobbyOwnerId) {
        final PeerId oldOwner = this.lobbyOwnerId;
        this.lobbyOwnerId = lobbyOwnerId;
        this.firePropertyChange(LOBBY_OWNER_PROPERTY, oldOwner, lobbyOwnerId);
    }

    public void setParticipantNames(final List<String> participants) {
        final List<String> oldParticipants = this.participantNames;
        this.participantNames = List.copyOf(participants);
        this.firePropertyChange(PARTICIPANT_NAMES_PROPERTY, oldParticipants, this.participantNames, true);
    }

    public void setMaxParticipantCount(final int maxParticipantCount) {
        final int oldCount = this.maxParticipantCount;
        this.maxParticipantCount = maxParticipantCount;
        this.firePropertyChange(MAX_PARTICIPANT_COUNT_PROPERTY, oldCount, maxParticipantCount);
    }
}
