package org.albard.dubito.lobby.app.demoViewer.views;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.albard.dubito.lobby.app.demoViewer.models.CurrentLobbyModel;
import org.albard.dubito.lobby.app.demoViewer.models.LobbyStateModel;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.dubito.utils.BoundComponentFactory;
import org.albard.dubito.utils.SimpleComponentFactory;

import com.jgoodies.binding.beans.BeanAdapter;

public final class CurrentLobbyView extends JPanel {
    private final Set<Runnable> exitLobbyListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<LobbyInfo>> saveLobbyInfoListeners = Collections.synchronizedSet(new HashSet<>());

    public CurrentLobbyView(final CurrentLobbyModel model, final LobbyStateModel stateModel) {
        this.setLayout(new BorderLayout(4, 4));
        final BeanAdapter<CurrentLobbyModel> modelAdapter = new BeanAdapter<>(model, true);
        final BeanAdapter<LobbyStateModel> stateModelAdapter = new BeanAdapter<>(stateModel, true);
        final JButton backButton = new JButton("< Back");
        final JTextField editableLobbyNameField = BoundComponentFactory.createStringTextField(modelAdapter,
                CurrentLobbyModel.LOBBY_NAME_PROPERTY);
        final JPasswordField editableLobbyPasswordField = BoundComponentFactory.createPasswordField(modelAdapter,
                CurrentLobbyModel.LOBBY_PASSWORD_PROPERTY);
        final JButton saveInfoButton = new JButton("Save");
        final JLabel readOnlyLobbyNameLabel = BoundComponentFactory.createStringLabel(modelAdapter,
                CurrentLobbyModel.LOBBY_NAME_PROPERTY);
        final JComponent adminPanel = SimpleComponentFactory.createHorizontalPanel(new JLabel("Lobby Name:"),
                editableLobbyNameField, new JLabel("Password:"), editableLobbyPasswordField, saveInfoButton);
        final JList<String> participantList = BoundComponentFactory.createList(modelAdapter,
                CurrentLobbyModel.PARTICIPANTS_PROPERTY);
        participantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(SimpleComponentFactory.createHorizontalPanel(backButton, readOnlyLobbyNameLabel, adminPanel),
                BorderLayout.NORTH);
        this.add(participantList, BorderLayout.CENTER);
        modelAdapter.addBeanPropertyChangeListener(CurrentLobbyModel.LOBBY_OWNER_PROPERTY, e -> {
            adminPanel.setVisible(model.isLocalPeerOwner());
            readOnlyLobbyNameLabel.setVisible(!model.isLocalPeerOwner());
        });
        stateModelAdapter.addBeanPropertyChangeListener(LobbyStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewValue() == LobbyStateModel.State.IN_LOBBY));
        saveInfoButton.addActionListener(e -> {
            final LobbyInfo newInfo = new LobbyInfo(model.getLobbyName(), model.getLobbyPassword());
            this.saveLobbyInfoListeners.forEach(l -> l.accept(newInfo));
        });
        backButton.addActionListener(e -> this.exitLobbyListeners.forEach(l -> l.run()));
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
}
