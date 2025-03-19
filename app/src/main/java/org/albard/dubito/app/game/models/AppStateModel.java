package org.albard.dubito.app.game.models;

import com.jgoodies.binding.beans.Model;

public final class AppStateModel extends Model {
    public enum State {
        IN_MAIN_MENU(true), REQUESTING_LOBBY_SERVER(true), IN_LOBBY_LIST(false), REQUESTING_LOBBY_PASSWORD(false),
        IN_LOBBY(false);

        private boolean isBeforeLobby;

        private State(boolean isBeforeLobby) {
            this.isBeforeLobby = isBeforeLobby;
        }

        public boolean isBeforeLobby() {
            return this.isBeforeLobby;
        }

        public static boolean isTransitionFromBeforeLobby(final State fromState, final State toState) {
            return fromState.isBeforeLobby() && !toState.isBeforeLobby();
        }

        public static boolean isTransitionToBeforeLobby(final State fromState, final State toState) {
            return !fromState.isBeforeLobby() && toState.isBeforeLobby();
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
