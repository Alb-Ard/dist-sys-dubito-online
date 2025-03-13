package org.albard.dubito.app.game.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.albard.dubito.app.game.models.JoinProtectedLobbyModel;
import org.albard.dubito.app.game.models.AppStateModel;
import org.albard.dubito.lobby.models.LobbyId;
import org.albard.dubito.utils.BoundComponentFactory;
import org.albard.dubito.utils.SimpleComponentFactory;

import com.jgoodies.binding.beans.BeanAdapter;

public final class JoinProtectedLobbyView extends JPanel {
    private final Set<BiConsumer<LobbyId, String>> joinListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Runnable> cancelListeners = Collections.synchronizedSet(new HashSet<>());

    public JoinProtectedLobbyView(final JoinProtectedLobbyModel model, final AppStateModel stateModel) {
        this.setSize(300, 200);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final BeanAdapter<JoinProtectedLobbyModel> modelAdapter = new BeanAdapter<>(model, true);
        final BeanAdapter<AppStateModel> stateModelAdapter = new BeanAdapter<>(stateModel, true);
        final JPasswordField passwordFiled = BoundComponentFactory.createPasswordField(modelAdapter,
                JoinProtectedLobbyModel.PASSWORD_PROPERTY);
        final JButton joinButton = new JButton("Join >");
        final JButton cancelButton = new JButton("< Cancel");
        this.add(passwordFiled);
        this.add(SimpleComponentFactory.createHorizontalPanel(cancelButton, joinButton));
        stateModelAdapter.addBeanPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewValue() == AppStateModel.State.REQUESTING_LOBBY_PASSWORD));
        joinButton.addActionListener(
                e -> this.joinListeners.forEach(l -> l.accept(model.getLobbyId(), model.getPassword())));
        cancelButton.addActionListener(e -> this.cancelListeners.forEach(l -> l.run()));
    }

    public void addJoinListener(final BiConsumer<LobbyId, String> listener) {
        this.joinListeners.add(listener);
    }

    public void removeJoinListener(final BiConsumer<LobbyId, String> listener) {
        this.joinListeners.remove(listener);
    }

    public void addCancelListener(final Runnable listener) {
        this.cancelListeners.add(listener);
    }

    public void removeCancelListener(final Runnable listener) {
        this.cancelListeners.remove(listener);
    }
}
