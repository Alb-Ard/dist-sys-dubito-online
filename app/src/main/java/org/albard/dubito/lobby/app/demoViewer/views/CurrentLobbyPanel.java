package org.albard.dubito.lobby.app.demoViewer.views;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;

import org.albard.dubito.lobby.app.demoViewer.models.CurrentLobbyModel;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.utils.AbstractDocumentListener;

public final class CurrentLobbyPanel extends JPanel {
    private final JTextField editableLobbyName = new JTextField();
    private final JLabel readOnlyLobbyName = new JLabel();
    private final JButton saveInfoButton = new JButton("Save");

    private final Set<Runnable> exitLobbyListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<LobbyInfo>> saveLobbyInfoListeners = Collections.synchronizedSet(new HashSet<>());

    private final CurrentLobbyModel model;
    private final DefaultListModel<String> participantListModel = new DefaultListModel<>();

    public CurrentLobbyPanel(final CurrentLobbyModel model) {
        this.setLayout(new BorderLayout(4, 4));
        this.model = model;
        this.model.addPropertyChangeListener(e -> this.updateFromCurrentModel());

        final JButton backButton = new JButton("< Back");
        backButton.addActionListener(e -> this.exitLobbyListeners.forEach(l -> l.run()));
        editableLobbyName.getDocument().addDocumentListener(new AbstractDocumentListener() {
            public void changedUpdate(final DocumentEvent e) {
                CurrentLobbyPanel.this.model.mapEditedLobbyInfo(i -> {
                    try {
                        return new LobbyInfo(e.getDocument().getText(0, e.getLength()), i.password());
                    } catch (final BadLocationException ex) {
                        return i;
                    }
                });
            }
        });
        this.saveInfoButton.addActionListener(e -> this.model.getEditedLobbyInfo()
                .ifPresent(i -> this.saveLobbyInfoListeners.forEach(l -> l.accept(i))));
        this.add(createTopBar(backButton, this.editableLobbyName, this.readOnlyLobbyName, this.saveInfoButton),
                BorderLayout.NORTH);
        final JList<String> participantList = new JList<String>(this.participantListModel);
        participantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(participantList);
    }

    public void addExitLobbyListener(final Runnable listener) {
        this.exitLobbyListeners.add(listener);
    }

    public void removeExitLobbyListener(final Runnable listener) {
        this.exitLobbyListeners.remove(listener);
    }

    public void addSaveLobbyInfoListener(final Consumer<LobbyInfo> listener) {
        this.saveLobbyInfoListeners.add(listener);
    }

    public void removeSaveLobbyInfoListener(final Consumer<LobbyInfo> listener) {
        this.saveLobbyInfoListeners.remove(listener);
    }

    private void updateFromCurrentModel() {
        this.model.getCurrentLobby().ifPresent(l -> {
            final boolean isReadOnly = !this.model.isLocalPeerOwner();
            this.editableLobbyName.setText(l.getInfo().name());
            this.readOnlyLobbyName.setText(l.getInfo().name());
            this.editableLobbyName.setVisible(!isReadOnly);
            this.saveInfoButton.setVisible(!isReadOnly);
            this.readOnlyLobbyName.setVisible(isReadOnly);
            this.participantListModel.clear();
            this.participantListModel.addAll(l.getParticipants().stream().map(p -> p.id()).toList());
        });
    }

    private static JComponent createTopBar(final JButton backButton, final JComponent... elements) {
        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(backButton);
        for (final JComponent element : elements) {
            container.add(element);
        }
        return container;
    }
}