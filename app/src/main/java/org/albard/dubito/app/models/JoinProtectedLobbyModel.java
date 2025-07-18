package org.albard.dubito.app.models;

import org.albard.dubito.lobby.models.LobbyId;
import org.albard.mvc.AbstractModel;
import org.albard.mvc.ModelProperty;

public final class JoinProtectedLobbyModel extends AbstractModel<JoinProtectedLobbyModel> {
    public static final ModelProperty<LobbyId> LOBBY_ID_PROPERTY = defineProperty("lobbyId");
    public static final ModelProperty<String> PASSWORD_PROPERTY = defineProperty("password");

    private LobbyId lobbyId;
    private String password;

    public LobbyId getLobbyId() {
        return this.lobbyId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setLobbyId(final LobbyId lobbyId) {
        this.firePropertyChange(LOBBY_ID_PROPERTY, this.lobbyId, () -> this.lobbyId = lobbyId);
    }

    public void setPassword(final String password) {
        this.firePropertyChange(PASSWORD_PROPERTY, this.password, () -> this.password = password);
    }
}
