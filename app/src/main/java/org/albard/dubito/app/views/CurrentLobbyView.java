package org.albard.dubito.app.views;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.albard.dubito.app.models.CurrentLobbyModel;
import org.abianchi.dubito.app.gameSession.views.GameButton;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.lobby.models.LobbyInfo;
import org.albard.mvc.BoundComponentFactory;
import org.albard.mvc.ExecutableViewCommand;
import org.albard.mvc.SimpleComponentFactory;
import org.albard.mvc.ViewCommand;

public final class CurrentLobbyView extends JPanel {
    private final ExecutableViewCommand<Runnable> exitLobbyCommand = new ExecutableViewCommand<>();
    private final ExecutableViewCommand<Consumer<LobbyInfo>> saveLobbyInfoCommand = new ExecutableViewCommand<>();
    private final ExecutableViewCommand<Runnable> startGameCommand = new ExecutableViewCommand<>();

    public CurrentLobbyView(final CurrentLobbyModel model, final AppStateModel stateModel) {
        this.setLayout(new BorderLayout(4, 4));
        final JButton backButton = new GameButton("< Back");
        final JTextField editableLobbyNameField = BoundComponentFactory.createStringTextField(model,
                CurrentLobbyModel.LOBBY_NAME_PROPERTY);
        final JPasswordField editableLobbyPasswordField = BoundComponentFactory.createPasswordField(model,
                CurrentLobbyModel.LOBBY_PASSWORD_PROPERTY);
        final JButton saveInfoButton = new GameButton("Save");
        final JButton startButton = new GameButton("Start!");
        final JLabel readOnlyLobbyNameLabel = BoundComponentFactory.createStringLabel(model,
                CurrentLobbyModel.LOBBY_NAME_PROPERTY);
        final JComponent adminPanel = SimpleComponentFactory.createHorizontalPanel(new JLabel("Lobby Name:"),
                editableLobbyNameField, new JLabel("Password:"), editableLobbyPasswordField, saveInfoButton,
                startButton);
        final JList<String> participantList = BoundComponentFactory.createList(model,
                CurrentLobbyModel.PARTICIPANT_NAMES_PROPERTY);
        participantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(
                SimpleComponentFactory.createVerticalPanel(backButton,
                        SimpleComponentFactory.createHorizontalPanel(readOnlyLobbyNameLabel, adminPanel)),
                BorderLayout.NORTH);
        this.add(participantList, BorderLayout.CENTER);
        model.addModelPropertyChangeListener(CurrentLobbyModel.LOBBY_OWNER_PROPERTY, e -> {
            adminPanel.setVisible(model.isLocalPeerOwner());
            readOnlyLobbyNameLabel.setVisible(!model.isLocalPeerOwner());
        });
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewTypedValue() == AppStateModel.State.IN_LOBBY));
        saveInfoButton.addActionListener(e -> {
            final LobbyInfo newInfo = new LobbyInfo(model.getLobbyName(), model.getLobbyPassword());
            this.saveLobbyInfoCommand.execute(l -> l.accept(newInfo));
        });
        startButton.addActionListener(e -> this.startGameCommand.execute(l -> l.run()));
        backButton.addActionListener(e -> this.exitLobbyCommand.execute(l -> l.run()));
    }

    public ViewCommand<Runnable> getExitLobbyCommand() {
        return this.exitLobbyCommand;
    }

    public ViewCommand<Consumer<LobbyInfo>> getSaveLobbyInfoCommand() {
        return this.saveLobbyInfoCommand;
    }

    public ViewCommand<Runnable> getStartGameCommand() {
        return this.startGameCommand;
    }
}
