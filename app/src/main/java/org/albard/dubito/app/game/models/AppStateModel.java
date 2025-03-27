package org.albard.dubito.app.game.models;

import com.jgoodies.binding.beans.Model;

public final class AppStateModel extends Model {
    public enum State {
        IN_MAIN_MENU(false), REQUESTING_LOBBY_SERVER(false), IN_LOBBY_LIST(true), REQUESTING_LOBBY_PASSWORD(true),
        IN_LOBBY(true);

        private final boolean isConnected;

        private State(boolean isConnected) {
            this.isConnected = isConnected;
        }

        public boolean isConnected() {
            return this.isConnected;
        }

        public boolean isConnecting(final State toState) {
            return !this.isConnected() && toState.isConnected();
        }

        public boolean isDisconnecting(final State toState) {
            return this.isConnected() && !toState.isConnected();
        }
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
