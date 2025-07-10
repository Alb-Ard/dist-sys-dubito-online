package org.albard.dubito.app.controllers;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.abianchi.dubito.app.ClientGameApp;
import org.abianchi.dubito.app.GameApp;
import org.abianchi.dubito.app.OwnerGameApp;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.CurrentLobbyModel;
import org.albard.dubito.app.models.AppStateModel.State;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.Lobby;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.mvc.ModelPropertyChangeEvent;
import org.albard.utils.Logger;

public final class LobbyManagementController {
    private static final int DEFAULT_OWNER_BIND_PORT = 10100;

    private final AppStateModel stateModel;
    private final CurrentLobbyModel currentLobbyModel;
    private final Consumer<GameApp> gameStartedListener;

    public LobbyManagementController(final AppStateModel stateModel, final CurrentLobbyModel currentLobbyModel,
            final Consumer<GameApp> gameStartedListener) {
        this.stateModel = stateModel;
        this.currentLobbyModel = currentLobbyModel;
        this.gameStartedListener = gameStartedListener;
        this.stateModel.addModelPropertyChangeListener(AppStateModel.LOBBY_CLIENT_PROPERTY, this::onLobbyClientChanged,
                null);
    }

    public void saveLobbyInfo(final LobbyInfo newInfo) {
        this.stateModel.getLobbyClient().ifPresent(x -> x.requestSaveLobbyInfo(newInfo));
    }

    public void leaveLobby() {
        this.stateModel.getLobbyClient().ifPresent(x -> x.requestLeaveCurrentLobby());
    }

    public void startGame() {
        this.stateModel.getLobbyClient().ifPresent(x -> x.requestStartCurrentLobbyGame());
    }

    private void onLobbyClientChanged(final ModelPropertyChangeEvent<Optional<LobbyClient>> e) {
        e.getOldTypedValue().ifPresent(x -> {
            x.removeCurrentLobbyUpdatedListener(this::onCurrentLobbyUpdated);
            x.removeCurrentLobbyGameStartedListener(this::onGameStarted);
        });
        e.getNewTypedValue().ifPresent(x -> {
            x.addCurrentLobbyUpdatedListener(this::onCurrentLobbyUpdated);
            x.addCurrentLobbyGameStartedListener(this::onGameStarted);
        });
    }

    private void onCurrentLobbyUpdated(final Optional<Lobby> lobby) {
        SwingUtilities.invokeLater(() -> lobby.ifPresentOrElse(l -> {
            this.currentLobbyModel.setFromLobby(l, this::getPeerUserName);
            this.stateModel.setState(AppStateModel.State.IN_LOBBY);
        }, () -> {
            this.stateModel.setState(AppStateModel.State.IN_LOBBY_LIST);
        }));
    }

    private String getPeerUserName(final Lobby lobby, final PeerId peerId) {
        return this.stateModel.getUserClient()
                .map(userClient -> userClient.getUser(peerId).map(user -> new StringBuilder(user.name()))
                        .map(x -> x.append(userClient.getLocalUser().peerId().equals(peerId) ? " (you)" : ""))
                        .map(x -> x.append(lobby.getOwner().equals(peerId) ? " (owner)" : "").toString())
                        .orElse(peerId.id()))
                .orElse(peerId.id());
    }

    private void onGameStarted(final PeerEndPoint ownerEndPoint) {
        this.stateModel.getLobbyClient().flatMap(x -> x.getCurrentLobby()).flatMap(currentLobby -> {
            return this.stateModel.getLobbyNetwork().flatMap(network -> {
                return this.currentLobbyModel.getLocalPeerId().flatMap(localPeerId -> {
                    try {
                        final Set<PeerId> participantPeerIds = currentLobby.getParticipants();
                        if (this.currentLobbyModel.isLocalPeerOwner()) {
                            return Optional.of(new OwnerGameApp(localPeerId,
                                    PeerEndPoint.ofValues("0.0.0.0", DEFAULT_OWNER_BIND_PORT),
                                    participantPeerIds.size(), stateModel));
                        } else {
                            return Optional.of(new ClientGameApp(localPeerId, PeerEndPoint.ofValues("0.0.0.0", 0),
                                    PeerEndPoint.ofValues(ownerEndPoint.getHost(), DEFAULT_OWNER_BIND_PORT),
                                    participantPeerIds.size(), stateModel));
                        }
                    } catch (final Exception ex) {
                        Logger.logError("Could not start game: " + ex.getMessage());
                        return Optional.empty();
                    }
                });
            });
        }).ifPresentOrElse(app -> {
            app.addGameStartedListener(this::leaveLobby);
            stateModel.setState(State.IN_GAME);
            this.gameStartedListener.accept(app);
        }, () -> Logger.logError("Could not create GameApp, game start failed!"));
    }
}
