package org.albard.dubito.app.lobby.app.demoViewer.models;

import java.util.Optional;
import java.util.function.Function;

import org.albard.dubito.app.lobby.models.Lobby;
import org.albard.dubito.app.lobby.models.LobbyInfo;
import org.albard.dubito.app.network.PeerId;

public final class CurrentLobbyModel extends AbstractModel {
    public static final String CURRENT_LOBBY_PROPERTY_NAME = "currentLobby";
    public static final String EDITED_LOBBY_INFO_PROPERTY_NAME = "editedLobbyInfo";

    private PeerId localPeerId;
    private Optional<Lobby> currentLobby;
    private Optional<LobbyInfo> editedLobbyInfo;

    public CurrentLobbyModel(final PeerId localPeerId, final Optional<Lobby> currentLobby) {
        this.localPeerId = localPeerId;
        this.currentLobby = currentLobby;
        this.editedLobbyInfo = currentLobby.map(l -> l.getInfo());
    }

    public PeerId getLocalPeerId() {
        return this.localPeerId;
    }

    public Optional<Lobby> getCurrentLobby() {
        return this.currentLobby;
    }

    public Optional<LobbyInfo> getEditedLobbyInfo() {
        return this.editedLobbyInfo;
    }

    public boolean isLocalPeerOwner() {
        return this.getCurrentLobby().map(l -> this.getLocalPeerId().equals(l.getOwner())).orElse(false);
    }

    public void setCurrentLobby(final Optional<Lobby> currentLobby) {
        this.firePropertyChange(CURRENT_LOBBY_PROPERTY_NAME, this.currentLobby, currentLobby,
                x -> this.currentLobby = x);
        currentLobby.ifPresent(l -> this.setEditedLobbyInfo(Optional.of(l.getInfo())));
    }

    public void setEditedLobbyInfo(final Optional<LobbyInfo> editedLobbyInfo) {
        if (this.getCurrentLobby().isPresent()) {
            this.firePropertyChange(EDITED_LOBBY_INFO_PROPERTY_NAME, this.editedLobbyInfo, editedLobbyInfo,
                    x -> this.editedLobbyInfo = x);
        }
    }

    public void mapEditedLobbyInfo(final Function<LobbyInfo, LobbyInfo> mapper) {
        this.getCurrentLobby().ifPresent(l -> {
            this.firePropertyChange(EDITED_LOBBY_INFO_PROPERTY_NAME, this.editedLobbyInfo,
                    Optional.ofNullable(mapper.apply(this.getEditedLobbyInfo().orElse(l.getInfo()))),
                    x -> this.editedLobbyInfo = x);
        });
    }
}
