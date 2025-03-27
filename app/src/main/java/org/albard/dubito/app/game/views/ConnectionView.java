package org.albard.dubito.app.game.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.albard.dubito.app.game.models.ConnectionRequestModel;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.utils.BoundComponentFactory;
import org.albard.dubito.utils.SimpleComponentFactory;

import com.jgoodies.binding.beans.BeanAdapter;

public final class ConnectionView extends JPanel {
    private final Set<Consumer<PeerEndPoint>> connectionRequestListeners = Collections.synchronizedSet(new HashSet<>());

    public ConnectionView(final ConnectionRequestModel model) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final BeanAdapter<ConnectionRequestModel> modelAdapter = new BeanAdapter<>(model, true);
        final JTextField addressField = BoundComponentFactory.createStringTextField(modelAdapter,
                ConnectionRequestModel.ADDRESS_PROPERTY);
        final JTextField portField = BoundComponentFactory.createIntegerTextField(modelAdapter,
                ConnectionRequestModel.PORT_PROPERTY);
        final JButton connectButton = SimpleComponentFactory.createGameButton("Connect");
        this.add(SimpleComponentFactory.createHorizontalPanel(addressField, portField));
        this.add(connectButton);
        connectButton.addActionListener(e -> this.connectionRequestListeners
                .forEach(l -> l.accept(PeerEndPoint.createFromValues(model.getAddress(), model.getPort()))));
    }

    public void addConnectionRequestListener(final Consumer<PeerEndPoint> listener) {
        this.connectionRequestListeners.add(listener);
    }

    public void removeConnectionRequestListener(final Consumer<PeerEndPoint> listener) {
        this.connectionRequestListeners.remove(listener);
    }
}
