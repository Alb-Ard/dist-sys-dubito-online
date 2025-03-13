package org.albard.dubito.app.game.models;

import org.albard.dubito.lobby.models.LobbyId;

import com.jgoodies.binding.beans.Model;

public final class JoinProtectedLobbyModel extends Model {
    public static final String LOBBY_ID_PROPERTY = "lobbyId";
    public static final String PASSWORD_PROPERTY = "password";

    private LobbyId lobbyId;
    private String password;

    public LobbyId getLobbyId() {
        return this.lobbyId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setLobbyId(final LobbyId lobbyId) {
        final LobbyId oldLobbyId = this.lobbyId;
        this.lobbyId = lobbyId;
        this.firePropertyChange(LOBBY_ID_PROPERTY, oldLobbyId, lobbyId);
    }

    public void setPassword(final String password) {
        final String oldPassword = this.password;
        this.password = password;
        this.firePropertyChange(PASSWORD_PROPERTY, oldPassword, password);
    }
}
