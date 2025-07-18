package org.albard.dubito.app.models;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.network.PeerId;
import org.albard.mvc.AbstractModel;
import org.albard.mvc.ModelProperty;

import com.jgoodies.binding.beans.BeanAdapter;

public final class CurrentLobbyModel extends AbstractModel<CurrentLobbyModel> {
    public static final ModelProperty<String> LOBBY_NAME_PROPERTY = defineProperty("lobbyName");
    public static final ModelProperty<String> LOBBY_PASSWORD_PROPERTY = defineProperty("lobbyPassword");
    public static final ModelProperty<PeerId> LOBBY_OWNER_PROPERTY = defineProperty("lobbyOwnerId");
    public static final ModelProperty<List<String>> PARTICIPANT_NAMES_PROPERTY = defineProperty("participantNames");
    public static final ModelProperty<Integer> MAX_PARTICIPANT_COUNT_PROPERTY = defineProperty("maxParticipantCount");

    private final BeanAdapter<AppStateModel> stateModelAdapter;

    private String lobbyName = "";
    private String lobbyPassword = "";
    private PeerId lobbyOwnerId = new PeerId("");
    private List<String> participantNames = List.of();
    private int maxParticipantCount = 0;

    public CurrentLobbyModel(final AppStateModel stateModel) {
        this.stateModelAdapter = new BeanAdapter<>(stateModel, true);
    }

    public Optional<PeerId> getLocalPeerId() {
        return this.stateModelAdapter.getBean().getLobbyNetwork().map(x -> x.getLocalPeerId());
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
        return this.getLocalPeerId().map(x -> x.equals(this.lobbyOwnerId)).orElse(false);
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
        this.firePropertyChange(LOBBY_NAME_PROPERTY, this.lobbyName, () -> this.lobbyName = lobbyName);
    }

    public void setLobbyPassword(final String lobbyPassword) {
        this.firePropertyChange(LOBBY_PASSWORD_PROPERTY, this.lobbyPassword, () -> this.lobbyPassword = lobbyPassword);
    }

    public void setLobbyOwnerId(final PeerId lobbyOwnerId) {
        this.firePropertyChange(LOBBY_OWNER_PROPERTY, this.lobbyOwnerId, () -> this.lobbyOwnerId = lobbyOwnerId);
    }

    public void setParticipantNames(final List<String> participants) {
        this.firePropertyChange(PARTICIPANT_NAMES_PROPERTY, this.participantNames,
                () -> this.participantNames = participants, true);
    }

    public void setMaxParticipantCount(final int maxParticipantCount) {
        this.firePropertyChange(MAX_PARTICIPANT_COUNT_PROPERTY, this.maxParticipantCount,
                () -> this.maxParticipantCount = maxParticipantCount);
    }
}
