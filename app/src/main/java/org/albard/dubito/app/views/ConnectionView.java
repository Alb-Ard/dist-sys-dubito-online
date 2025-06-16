package org.albard.dubito.app.views;

import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.abianchi.dubito.app.gameSession.views.GameButton;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.app.models.ConnectionRequestModel;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.mvc.BoundComponentFactory;
import org.albard.mvc.ExecutableViewCommand;
import org.albard.mvc.SimpleComponentFactory;
import org.albard.mvc.ViewCommand;

public final class ConnectionView extends JPanel {
    private final ExecutableViewCommand<Consumer<PeerEndPoint>> connectCommand = new ExecutableViewCommand<>();

    public ConnectionView(final AppStateModel stateModel, final ConnectionRequestModel connectionModel) {
        this.setMinimumSize(new Dimension(300, 100));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JTextField addressField = BoundComponentFactory.createStringTextField(connectionModel,
                ConnectionRequestModel.ADDRESS_PROPERTY);
        final JTextField portField = BoundComponentFactory.createIntegerTextField(connectionModel,
                ConnectionRequestModel.PORT_PROPERTY);
        final JButton connectButton = new GameButton("Connect");
        this.add(SimpleComponentFactory.createHorizontalPanel(addressField, portField));
        this.add(connectButton);
        connectButton.addActionListener(e -> connectCommand.execute(
                l -> l.accept(PeerEndPoint.ofValues(connectionModel.getAddress(), connectionModel.getPort()))));
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewValue() == AppStateModel.State.IN_LOBBY_SERVER_CONNECTION));
    }

    public ViewCommand<Consumer<PeerEndPoint>> getConnectCommand() {
        return this.connectCommand;
    }
}