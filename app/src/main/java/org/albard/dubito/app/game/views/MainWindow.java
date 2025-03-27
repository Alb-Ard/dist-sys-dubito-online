package org.albard.dubito.app.game.views;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.abianchi.dubito.app.views.StartPane;
import org.albard.dubito.app.game.models.AppStateModel.State;
import org.albard.dubito.app.game.models.AppStateModel;
import org.albard.dubito.app.game.models.ConnectionRequestModel;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.utils.Navigator;
import org.albard.dubito.utils.Locked;

import com.jgoodies.binding.beans.BeanAdapter;

public final class MainWindow extends JFrame {
    private final AppStateModel stateModel = new AppStateModel();
    private final ConnectionRequestModel connectionModel = new ConnectionRequestModel("127.0.0.1", 9000);
    private final Locked<Optional<PeerNetwork>> network = Locked.of(Optional.empty());
    private final Consumer<Optional<PeerNetwork>> onNetworkChanged;
    private final Navigator<AppStateModel.State> navigator;

    public MainWindow() {
        super("Dubito Online");
        this.setMinimumSize(new Dimension(640, 480));
        this.navigator = new Navigator<>(this, x -> x.toString());
        final BeanAdapter<AppStateModel> modelAdapter = new BeanAdapter<>(this.stateModel, true);
        final ConnectionView connectionView = new ConnectionView(this.connectionModel);
        final MainLobbyView mainLobbyView = new MainLobbyView(this.stateModel);
        final StartPane mainMenuView = new StartPane(() -> this.stateModel.setState(State.REQUESTING_LOBBY_SERVER));
        this.navigator
                .addScreen(mainLobbyView, Set.of(State.IN_LOBBY, State.IN_LOBBY_LIST, State.REQUESTING_LOBBY_PASSWORD))
                .addScreen(connectionView, State.REQUESTING_LOBBY_SERVER).addScreen(mainMenuView, State.IN_MAIN_MENU);
        modelAdapter.addBeanPropertyChangeListener(AppStateModel.STATE_PROPERTY, e -> SwingUtilities.invokeLater(() -> {
            final State newState = (State) e.getNewValue();
            final State oldState = (State) e.getOldValue();
            System.out.println("App state: " + oldState + " -> " + newState);
            if (newState.isDisconnecting(oldState)) {
                this.closeNetwork();
            } else if (newState.isConnecting(oldState)) {
                if (!this.createNetwork()) {
                    System.err.println("Network creation FAIL!");
                    SwingUtilities.invokeLater(() -> this.stateModel.setState(oldState));
                    return;
                }
            }
            this.navigator.navigateTo(newState);
        }));
        connectionView.addConnectionRequestListener(e -> this.stateModel.setState(State.IN_LOBBY_LIST));
        this.onNetworkChanged = mainLobbyView::setNetwork;
        this.stateModel.setState(State.IN_MAIN_MENU);
        this.navigator.navigateTo(State.IN_MAIN_MENU);
    }

    @Override
    public void dispose() {
        this.closeNetwork();
    }

    private boolean createNetwork() {
        this.network.exchange(network -> {
            if (network.isPresent()) {
                return network;
            }
            try {
                System.out.println(
                        "Connecting to " + this.connectionModel.getAddress() + ":" + this.connectionModel.getPort());
                final PeerNetwork newNetwork = PeerNetwork.createBound(PeerId.createNew(), "0.0.0.0", 0,
                        new MessengerFactory(MessageSerializer.createJson()));
                if (newNetwork.connectToPeer(PeerEndPoint.createFromValues(this.connectionModel.getAddress(),
                        this.connectionModel.getPort()))) {
                    System.out.println("Connected!");
                    return Optional.ofNullable(newNetwork);
                } else {
                    System.err.println("Connection FAIL!");
                    return Optional.empty();
                }
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
            return network;
        });
        this.onNetworkChanged.accept(this.network.getValue());
        return this.network.getValue().isPresent();
    }

    private void closeNetwork() {
        System.out.println("Network closed");
        this.network.exchange(x -> {
            x.ifPresent(network -> {
                try {
                    network.close();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            });
            return Optional.empty();
        });
        this.onNetworkChanged.accept(this.network.getValue());
    }
}