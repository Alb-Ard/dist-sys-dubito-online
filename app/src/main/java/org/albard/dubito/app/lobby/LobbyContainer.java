package org.albard.dubito.app.lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.utils.Either;
import org.albard.dubito.app.utils.Locked;

public final class LobbyContainer {
    public static record LobbyResult(Lobby lobby, boolean isDeleted) {
    }

    public static class Result {
        private Either<List<String>, LobbyResult> value;

        public Result() {
            this.value = Either.ofX(new ArrayList<>());
        }

        public Either<List<String>, LobbyResult> getValue() {
            return this.value;
        }

        private void setErrors(final List<String> errors) {
            this.value = Either.ofX(errors);
        }

        private void setLobby(final Lobby lobby, final boolean isDeleted) {
            this.value = Either.ofY(new LobbyResult(lobby, isDeleted));
        }
    }

    private final Locked<Map<LobbyId, Lobby>> lobbies = Locked.of(new HashMap<>());

    public int getLobbyCount() {
        return this.lobbies.getValue().size();
    }

    public Map<LobbyId, Lobby> getLobbies() {
        return Map.copyOf(this.lobbies.getValue());
    }

    public Result createLobby(final PeerId owner, final LobbyInfo info) {
        final Result result = new Result();
        this.lobbies.exchange(lobbies -> {
            final List<Exception> validationErrors = info.validate();
            if (!validationErrors.isEmpty()) {
                result.setErrors(validationErrors.stream().map(e -> e.getMessage()).toList());
                return lobbies;
            }
            if (lobbies.values().stream().anyMatch(l -> l.getParticipants().contains(owner))) {
                result.setErrors(List.of("user already in a lobby"));
                return lobbies;
            }
            final Lobby newLobby = new Lobby(LobbyId.createNew(), owner, info);
            System.out.println(new StringBuilder().append("Creating new lobby \"").append(info.name())
                    .append("\" owned by \"").append(owner).append(getLobbyCount()).toString());
            lobbies.put(newLobby.getId(), newLobby);
            result.setLobby(newLobby, false);
            return lobbies;
        });
        return result;
    }

    public Result updateLobby(final LobbyId lobbyId, final PeerId editorId, final LobbyInfo newInfo) {
        final Result result = new Result();
        this.lobbies.exchange(lobbies -> {
            final Lobby lobbyToEdit = lobbies.get(lobbyId);
            if (lobbyToEdit == null) {
                result.setErrors(List.of("lobby not found"));
                return lobbies;
            }
            if (!lobbyToEdit.getOwner().equals(editorId)) {
                result.setErrors(List.of("sender is not the lobby owner"));
                return lobbies;
            }
            final List<Exception> validationErrors = newInfo.validate();
            if (!validationErrors.isEmpty()) {
                result.setErrors(validationErrors.stream().map(e -> e.getMessage()).toList());
                return lobbies;
            }
            final Lobby newLobby = lobbyToEdit.setInfo(newInfo);
            lobbies.put(lobbyId, newLobby);
            result.setLobby(newLobby, false);
            return lobbies;
        });
        return result;
    }

    public Result joinLobby(final LobbyId lobbyId, final PeerId joinerId, final String password) {
        final Result result = new Result();
        this.lobbies.exchange(lobbies -> {
            final Lobby lobbyToJoin = lobbies.get(lobbyId);
            if (lobbyToJoin == null) {
                result.setErrors(List.of("lobby not found"));
                return lobbies;
            }
            final String expectedPassword = lobbyToJoin.getInfo().password();
            if (!expectedPassword.isBlank() && !lobbyToJoin.getInfo().password().equals(password)) {
                result.setErrors(List.of("invalid password"));
                return lobbies;
            }
            if (lobbies.values().stream().anyMatch(l -> l.getParticipants().contains(joinerId))) {
                result.setErrors(List.of("user already in a lobby"));
                return lobbies;
            }
            final Lobby newLobby = lobbyToJoin.addParticipant(joinerId);
            lobbies.put(lobbyId, newLobby);
            result.setLobby(newLobby, false);
            return lobbies;
        });
        return result;
    }

    public Result leaveLobby(final LobbyId lobbyId, final PeerId leaverId) {
        final Result result = new Result();
        this.lobbies.exchange(lobbies -> {
            final Lobby lobbyToLeave = lobbies.get(lobbyId);
            if (lobbyToLeave == null) {
                result.setErrors(List.of("lobby not found"));
                return lobbies;
            }
            if (lobbies.values().stream().noneMatch(l -> l.getParticipants().contains(leaverId))) {
                result.setErrors(List.of("user is not in the given lobby"));
                return lobbies;
            }
            if (!leaverId.equals(lobbyToLeave.getOwner())) {
                final Lobby newLobby = lobbyToLeave.removeParticipant(leaverId);
                lobbies.put(lobbyId, newLobby);
                result.setLobby(newLobby, false);
                return lobbies;
            } else {
                final Lobby deletedLobby = lobbies.remove(lobbyId);
                result.setLobby(deletedLobby, true);
                return lobbies;
            }
        });
        return result;
    }
}
