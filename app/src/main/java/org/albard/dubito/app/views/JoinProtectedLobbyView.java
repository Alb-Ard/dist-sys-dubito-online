package org.albard.dubito.app.views;

import java.util.function.BiConsumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JLabel;

import org.albard.dubito.app.models.JoinProtectedLobbyModel;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.mvc.BoundComponentFactory;
import org.albard.mvc.ExecutableViewCommand;
import org.albard.mvc.SimpleComponentFactory;
import org.albard.mvc.ViewCommand;

public final class JoinProtectedLobbyView extends JPanel {
    private final ExecutableViewCommand<BiConsumer<LobbyId, String>> joinCommand = new ExecutableViewCommand<>();
    private final ExecutableViewCommand<Runnable> cancelCommand = new ExecutableViewCommand<>();

    public JoinProtectedLobbyView(final JoinProtectedLobbyModel model, final AppStateModel stateModel) {
        this.setSize(300, 200);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JLabel passwordLabel = new JLabel("Put lobby password here!");
        final JPasswordField passwordFiled = BoundComponentFactory.createPasswordField(model,
                JoinProtectedLobbyModel.PASSWORD_PROPERTY);
        final JButton joinButton = new JButton("Join >");
        final JButton cancelButton = new JButton("< Cancel");
        this.add(passwordLabel);
        this.add(passwordFiled);
        this.add(SimpleComponentFactory.createHorizontalPanel(cancelButton, joinButton));
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewTypedValue() == AppStateModel.State.REQUESTING_LOBBY_PASSWORD));
        joinButton.addActionListener(
                e -> this.joinCommand.execute(l -> l.accept(model.getLobbyId(), model.getPassword())));
        cancelButton.addActionListener(e -> this.cancelCommand.execute(l -> l.run()));
    }

    public ViewCommand<BiConsumer<LobbyId, String>> getJoinCommand() {
        return this.joinCommand;
    }

    public ViewCommand<Runnable> getCancelCommand() {
        return this.cancelCommand;
    }
}
