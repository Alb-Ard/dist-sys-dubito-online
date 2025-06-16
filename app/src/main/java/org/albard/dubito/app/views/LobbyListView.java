package org.albard.dubito.app.views;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.albard.dubito.app.models.CurrentUserModel;
import org.abianchi.dubito.app.gameSession.views.GameButton;
import org.albard.dubito.app.models.AppStateModel;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.mvc.BoundComponentFactory;
import org.albard.mvc.ExecutableViewCommand;
import org.albard.mvc.SimpleComponentFactory;
import org.albard.mvc.ViewCommand;

public final class LobbyListView extends JPanel {
    private final ExecutableViewCommand<Runnable> createLobbyCommand = new ExecutableViewCommand<>();
    private final ExecutableViewCommand<Consumer<LobbyDisplay>> lobbySelectedCommand = new ExecutableViewCommand<>();
    private final ExecutableViewCommand<Consumer<String>> saveUserNameCommand = new ExecutableViewCommand<>();

    public LobbyListView(final DefaultListModel<LobbyDisplay> listModel, final AppStateModel stateModel,
            final CurrentUserModel userModel) {
        this.setLayout(new BorderLayout(4, 4));
        final JLabel noLobbiesLabel = new JLabel("No lobbies found.");
        final JButton createLobbyButton = new GameButton("Create Lobby");
        final JComponent lobbyList = SimpleComponentFactory.createVerticalPanel();
        final JTextField userNameField = BoundComponentFactory.createStringTextField(userModel,
                CurrentUserModel.NAME_PROPERTY);
        final JButton saveUserButton = new GameButton("Set Username");
        this.add(noLobbiesLabel, BorderLayout.NORTH);
        this.add(lobbyList, BorderLayout.CENTER);
        this.add(
                SimpleComponentFactory.createVerticalPanel(createLobbyButton, new JSeparator(JSeparator.HORIZONTAL),
                        SimpleComponentFactory.createHorizontalPanel(userNameField, saveUserButton)),
                BorderLayout.SOUTH);
        listModel.addListDataListener(this.createLobbyListDataListener(noLobbiesLabel, listModel, lobbyList));
        stateModel.addModelPropertyChangeListener(AppStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewValue() == AppStateModel.State.IN_LOBBY_LIST));
        createLobbyButton.addActionListener(e -> this.createLobbyCommand.execute(l -> l.run()));
        saveUserButton.addActionListener(e -> this.saveUserNameCommand.execute(l -> l.accept(userModel.getName())));
    }

    private ListDataListener createLobbyListDataListener(final JLabel noLobbiesLabel,
            final DefaultListModel<LobbyDisplay> listModel, final JComponent lobbyList) {
        return new ListDataListener() {
            @Override
            public void intervalAdded(final ListDataEvent e) {
                this.repaint();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                this.repaint();
            }

            @Override
            public void contentsChanged(final ListDataEvent e) {
                this.repaint();
            }

            private void repaint() {
                noLobbiesLabel.setVisible(listModel.getSize() <= 0);
                lobbyList.removeAll();
                for (int i = 0; i < listModel.size(); i++) {
                    final LobbyListItemView item = new LobbyListItemView(listModel.get(i));
                    item.getLobbySelectedCommand()
                            .addListener(x -> LobbyListView.this.lobbySelectedCommand.execute(l -> l.accept(x)));
                    lobbyList.add(item);
                }
            }
        };
    }

    public ViewCommand<Consumer<LobbyDisplay>> getLobbySelectedCommand() {
        return this.lobbySelectedCommand;
    }

    public ViewCommand<Consumer<String>> getSaveUserNameCommand() {
        return this.saveUserNameCommand;
    }

    public ViewCommand<Runnable> getCreateLobbyCommand() {
        return this.createLobbyCommand;
    }
}
