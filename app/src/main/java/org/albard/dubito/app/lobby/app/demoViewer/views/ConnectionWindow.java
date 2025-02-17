package org.albard.dubito.app.lobby.app.demoViewer.views;

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;

import org.albard.dubito.app.lobby.app.demoViewer.models.ConnectionRequestModel;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.utils.AbstractDocumentListener;

public final class ConnectionWindow extends JFrame {
    private final JTextField addressField = new JTextField();
    private final JTextField portField = new JTextField();
    private final Set<Consumer<PeerEndPoint>> connectionRequestListeners = Collections.synchronizedSet(new HashSet<>());

    private final ConnectionRequestModel model;

    public ConnectionWindow(final ConnectionRequestModel model) {
        super("Connect to Lobby Server");
        this.getRootPane().setLayout(new BoxLayout(this.getRootPane(), BoxLayout.Y_AXIS));
        this.model = model;
        this.addressField.getDocument().addDocumentListener(new AbstractDocumentListener() {
            public void changedUpdate(final DocumentEvent e) {
                try {
                    ConnectionWindow.this.model.setAddress(e.getDocument().getText(0, e.getLength()));
                } catch (final BadLocationException ex) {
                }
            }
        });
        this.portField.getDocument().addDocumentListener(new AbstractDocumentListener() {
            public void changedUpdate(final DocumentEvent e) {
                try {
                    ConnectionWindow.this.model.setPort(Integer.parseInt(e.getDocument().getText(0, e.getLength())));
                } catch (final BadLocationException ex) {
                }
            }
        });
        this.model.addPropertyChangeListener(ConnectionRequestModel.ADDRESS_PROPERTY_NAME,
                e -> this.updateFromCurrentModel());
        this.model.addPropertyChangeListener(ConnectionRequestModel.PORT_PROPERTY_NAME,
                e -> this.updateFromCurrentModel());
        this.updateFromCurrentModel();
        this.getRootPane().add(createFieldsSection(addressField, portField));
        final JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> this.connectionRequestListeners
                .forEach(l -> l.accept(PeerEndPoint.createFromValues(model.getAddress(), model.getPort()))));
        this.getRootPane().add(connectButton);
        this.setMinimumSize(new Dimension(300, 100));
    }

    public void addConnectionRequestListener(final Consumer<PeerEndPoint> listener) {
        this.connectionRequestListeners.add(listener);
    }

    public void removeConnectionRequestListener(final Consumer<PeerEndPoint> listener) {
        this.connectionRequestListeners.remove(listener);
    }

    private void updateFromCurrentModel() {
        this.addressField.setText(this.model.getAddress());
        this.portField.setText(Integer.toString(this.model.getPort()));
    }

    private static JComponent createFieldsSection(final JTextField addressField, final JTextField portField) {
        final JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.X_AXIS));
        section.add(addressField);
        section.add(portField);
        return section;
    }
}