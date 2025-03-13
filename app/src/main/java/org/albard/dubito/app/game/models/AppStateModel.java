package org.albard.dubito.app.game.models;

import com.jgoodies.binding.beans.Model;

public final class AppStateModel extends Model {
    public enum State {
        IN_MAIN_MENU, REQUESTING_LOBBY_SERVER, IN_LOBBY_LIST, REQUESTING_LOBBY_PASSWORD, IN_LOBBY
    }

    public static final String STATE_PROPERTY = "state";

    private State state = State.IN_MAIN_MENU;

    public State getState() {
        return this.state;
    }

    public void setState(final State state) {
        final State oldState = this.state;
        this.state = state;
        this.firePropertyChange(STATE_PROPERTY, oldState, state);
    }
}
