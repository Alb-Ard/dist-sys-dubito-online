package org.albard.dubito.app.models;

import java.util.Optional;

import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.client.UserClient;
import org.albard.mvc.AbstractModel;
import org.albard.mvc.ModelProperty;

public final class AppStateModel extends AbstractModel<AppStateModel> {
    public enum State {
        IN_MAIN_MENU, IN_LOBBY_SERVER_CONNECTION, IN_LOBBY_LIST, REQUESTING_LOBBY_PASSWORD, IN_LOBBY, IN_GAME
    }

    public static final ModelProperty<State> STATE_PROPERTY = defineProperty("state");
    public static final ModelProperty<Optional<PeerNetwork>> NETWORK_PROPERTY = defineProperty("network");
    public static final ModelProperty<Optional<UserClient>> USER_CLIENT_PROPERTY = defineProperty("userClient");
    public static final ModelProperty<Optional<LobbyClient>> LOBBY_CLIENT_PROPERTY = defineProperty("lobbyClient");

    private State state;
    private Optional<PeerNetwork> network = Optional.empty();
    private Optional<UserClient> userClient = Optional.empty();
    private Optional<LobbyClient> lobbyClient = Optional.empty();

    public State getState() {
        return this.state;
    }

    public Optional<PeerNetwork> getNetwork() {
        return this.network;
    }

    public Optional<UserClient> getUserClient() {
        return this.userClient;
    }

    public Optional<LobbyClient> getLobbyClient() {
        return this.lobbyClient;
    }

    public void setState(final State state) {
        this.firePropertyChange(STATE_PROPERTY, this.state, () -> this.state = state);
    }

    public void setNetwork(final PeerNetwork network) {
        this.firePropertyChange(NETWORK_PROPERTY, this.network, () -> this.network = Optional.of(network));
        this.firePropertyChange(USER_CLIENT_PROPERTY, this.userClient,
                () -> this.userClient = Optional.of(new UserClient(network)));
        this.firePropertyChange(LOBBY_CLIENT_PROPERTY, this.lobbyClient,
                () -> this.lobbyClient = Optional.of(new LobbyClient(network)));
    }
}
