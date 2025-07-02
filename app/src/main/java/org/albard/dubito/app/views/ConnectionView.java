package org.albard.dubito.app.views;

import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.Box;
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
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JTextField addressField = BoundComponentFactory.createStringTextField(connectionModel,
                ConnectionRequestModel.ADDRESS_PROPERTY);
        final JTextField portField = BoundComponentFactory.createStringTextField(connectionModel,
                ConnectionRequestModel.PORT_PROPERTY);
        final JButton connectButton = new GameButton("Connect");
        this.add(Box.createVerticalGlue());
        this.add(SimpleComponentFactory.createHorizontalPanel(Box.createHorizontalGlue(), addressField, portField,
                Box.createHorizontalGlue()));
        this.add(Box.createVerticalStrut(8));
        this.add(SimpleComponentFactory.createHorizontalPanel(Box.createHorizontalGlue(), connectButton,
                Box.createHorizontalGlue()));
        this.add(Box.createVerticalGlue());
        connectButton.addActionListener(e -> tryParseInt(connectionModel.getPort()).ifPresent(port -> connectCommand
                .execute(l -> l.accept(PeerEndPoint.ofValues(connectionModel.getAddress(), port)))));
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewTypedValue() == AppStateModel.State.IN_LOBBY_SERVER_CONNECTION));
    }

    public ViewCommand<Consumer<PeerEndPoint>> getConnectCommand() {
        return this.connectCommand;
    }

    private static Optional<Integer> tryParseInt(final String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (final Exception ex) {
            return Optional.empty();
        }
    }
}