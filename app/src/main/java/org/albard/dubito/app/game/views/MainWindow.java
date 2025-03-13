package org.albard.dubito.app.game.views;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.abianchi.dubito.app.views.StartPane;
import org.albard.dubito.app.game.controllers.LobbyController;
import org.albard.dubito.app.game.models.CurrentLobbyModel;
import org.albard.dubito.app.game.models.CurrentUserModel;
import org.albard.dubito.app.game.models.JoinProtectedLobbyModel;
import org.albard.dubito.app.game.models.AppStateModel.State;
import org.albard.dubito.app.game.models.AppStateModel;
import org.albard.dubito.app.game.models.ConnectionRequestModel;
import org.albard.dubito.lobby.client.LobbyClient;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.client.UserClient;

import com.jgoodies.binding.beans.BeanAdapter;

public final class MainWindow extends JFrame {
    private final AppStateModel stateModel = new AppStateModel();
    private final ConnectionRequestModel connectionModel = new ConnectionRequestModel("127.0.0.1", 9000);
    private Optional<PeerNetwork> network = Optional.empty();

    public MainWindow() {
        super("Dubito Online");
        this.setMinimumSize(new Dimension(640, 480));
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        final BeanAdapter<AppStateModel> modelAdapter = new BeanAdapter<>(this.stateModel, true);
        modelAdapter.addBeanPropertyChangeListener(AppStateModel.STATE_PROPERTY, e -> SwingUtilities.invokeLater(() -> {
            switch ((State) e.getNewValue()) {
            case IN_MAIN_MENU:
                this.showMainMenu();
                break;

            case REQUESTING_LOBBY_SERVER:
                this.showConnectionMenu();
                break;

            default:
                this.showLobbyList();
                break;
            }
        }));
        this.stateModel.setState(State.IN_MAIN_MENU);
        this.showMainMenu();
    }

    private void showMainMenu() {
        final StartPane startMenuView = new StartPane(() -> this.stateModel.setState(State.REQUESTING_LOBBY_SERVER));
        this.getContentPane().removeAll();
        this.getContentPane().add(startMenuView);
        this.repaint();
    }

    private void showConnectionMenu() {
        final ConnectionView connectionView = new ConnectionView(this.connectionModel);
        connectionView.addConnectionRequestListener(e -> this.stateModel.setState(State.IN_LOBBY_LIST));
        this.getContentPane().removeAll();
        this.getContentPane().add(connectionView);
        this.repaint();
    }

    private void showLobbyList() {
        if (this.network.isPresent()) {
            return;
        }
        try {
            final PeerNetwork newNetwork = PeerNetwork.createBound(PeerId.createNew(), "0.0.0.0", 0,
                    new MessengerFactory(MessageSerializer.createJson()));
            if (newNetwork.connectToPeer(
                    PeerEndPoint.createFromValues(this.connectionModel.getAddress(), this.connectionModel.getPort()))) {
                this.network = Optional.ofNullable(newNetwork);
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        if (this.network.isEmpty()) {
            this.stateModel.setState(State.REQUESTING_LOBBY_SERVER);
            return;
        }
        final LobbyClient lobbyClient = new LobbyClient(this.network.get());
        final UserClient userClient = new UserClient(this.network.get());
        final DefaultListModel<LobbyDisplay> lobbyListModel = new DefaultListModel<>();
        final CurrentLobbyModel currentLobbyModel = new CurrentLobbyModel(lobbyClient.getLocalPeerId());
        final JoinProtectedLobbyModel joinLobbyModel = new JoinProtectedLobbyModel();
        final CurrentUserModel userModel = new CurrentUserModel(userClient.getLocalUser().name());
        final LobbyController controller = new LobbyController(lobbyClient, userClient, currentLobbyModel,
                lobbyListModel, joinLobbyModel, stateModel, userModel);
        final JoinProtectedLobbyView joinLobbyView = new JoinProtectedLobbyView(joinLobbyModel, stateModel);
        final LobbyListView lobbyListView = new LobbyListView(lobbyListModel, stateModel, userModel);
        final CurrentLobbyView currentLobbyView = new CurrentLobbyView(currentLobbyModel, stateModel);
        this.getContentPane().removeAll();
        this.getContentPane().add(lobbyListView);
        this.getContentPane().add(currentLobbyView);
        this.getContentPane().add(joinLobbyView);
        this.repaint();
        lobbyListView.addCreateLobbyListener(() -> controller.createLobby(new LobbyInfo("New Lobby", "")));
        lobbyListView.addLobbySelectedListener(controller::joinLobby);
        lobbyListView.addSaveUserNameListener(controller::setName);
        currentLobbyView.addSaveLobbyInfoListener(controller::saveLobbyInfo);
        currentLobbyView.addExitLobbyListener(controller::leaveLobby);
        joinLobbyView.addJoinListener(controller::joinProtectedLobby);
        joinLobbyView.addCancelListener(controller::cancelJoinProtectedLobby);
    }

    @Override
    public void dispose() {
        this.network.ifPresent(network -> {
            try {
                network.close();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}