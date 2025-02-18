package org.albard.dubito.lobby.app.demoViewer.views;

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.albard.dubito.lobby.app.demoViewer.models.ConnectionRequestModel;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.utils.BoundComponentFactory;
import org.albard.dubito.utils.SimpleComponentFactory;

import com.jgoodies.binding.beans.BeanAdapter;

public final class ConnectionWindow extends JFrame {
    private final Set<Consumer<PeerEndPoint>> connectionRequestListeners = Collections.synchronizedSet(new HashSet<>());

    public ConnectionWindow(final ConnectionRequestModel model) {
        super("Connect to Lobby Server");
        this.setMinimumSize(new Dimension(300, 100));
        this.getRootPane().setLayout(new BoxLayout(this.getRootPane(), BoxLayout.Y_AXIS));
        final BeanAdapter<ConnectionRequestModel> modelAdapter = new BeanAdapter<>(model, true);
        final JTextField addressField = BoundComponentFactory.createStringTextField(modelAdapter,
                ConnectionRequestModel.ADDRESS_PROPERTY);
        final JTextField portField = BoundComponentFactory.createIntegerTextField(modelAdapter,
                ConnectionRequestModel.PORT_PROPERTY);
        final JButton connectButton = new JButton("Connect");
        this.getRootPane().add(SimpleComponentFactory.createHorizontalPanel(addressField, portField));
        this.getRootPane().add(connectButton);
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