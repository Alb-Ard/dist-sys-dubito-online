package org.albard.dubito.lobby.app.demoViewer.models;

import com.jgoodies.binding.beans.Model;

public final class LobbyStateModel extends Model {
    public enum State {
        IN_LIST, REQUESTING_PASSWORD, IN_LOBBY
    }

    public static final String STATE_PROPERTY = "state";

    private State state;

    public State getState() {
        return this.state;
    }

    public void setState(final State state) {
        final State oldState = this.state;
        this.state = state;
        this.firePropertyChange(STATE_PROPERTY, oldState, state);
    }
}
