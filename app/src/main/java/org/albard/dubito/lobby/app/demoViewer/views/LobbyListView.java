package org.albard.dubito.lobby.app.demoViewer.views;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.albard.dubito.lobby.app.demoViewer.models.LobbyStateModel;
import org.albard.dubito.lobby.models.LobbyDisplay;

import com.jgoodies.binding.beans.BeanAdapter;

public final class LobbyListView extends JPanel {
    private final JLabel noLobbiesLabel = new JLabel("No lobbies found.");

    private final Set<Runnable> createLobbyListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<LobbyDisplay>> lobbySelectedListeners = Collections.synchronizedSet(new HashSet<>());

    private final DefaultListModel<LobbyDisplay> model;

    public LobbyListView(final DefaultListModel<LobbyDisplay> listModel, final LobbyStateModel stateModel) {
        this.setLayout(new BorderLayout(4, 4));
        this.model = listModel;
        final BeanAdapter<LobbyStateModel> stateModelAdapter = new BeanAdapter<>(stateModel, true);
        final JButton createLobbyButton = new JButton("Create Lobby");
        final JPanel lobbyList = new JPanel();
        this.add(this.noLobbiesLabel, BorderLayout.NORTH);
        this.add(lobbyList, BorderLayout.CENTER);
        this.add(createLobbyButton, BorderLayout.SOUTH);
        this.model.addListDataListener(new ListDataListener() {
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
                LobbyListView.this.noLobbiesLabel.setVisible(listModel.getSize() <= 0);
                lobbyList.removeAll();
                for (int i = 0; i < listModel.getSize(); i++) {
                    final LobbyListItemView item = new LobbyListItemView();
                    item.setLobby(listModel.get(i));
                    item.addLobbySelectedListener(
                            x -> LobbyListView.this.lobbySelectedListeners.forEach(l -> l.accept(x)));
                    lobbyList.add(item);
                }
            }
        });
        stateModelAdapter.addBeanPropertyChangeListener(LobbyStateModel.STATE_PROPERTY,
                e -> this.setVisible(e.getNewValue() == LobbyStateModel.State.IN_LIST));
        createLobbyButton.addActionListener(e -> this.createLobbyListeners.forEach(l -> l.run()));
    }

    public void addLobbySelectedListener(final Consumer<LobbyDisplay> listener) {
        this.lobbySelectedListeners.add(listener);
    }

    public void removeLobbySelectedListener(final Consumer<LobbyDisplay> listener) {
        this.lobbySelectedListeners.remove(listener);
    }

    public void addCreateLobbyListener(final Runnable listener) {
        this.createLobbyListeners.add(listener);
    }

    public void removeCreateLobbyListener(final Runnable listener) {
        this.createLobbyListeners.remove(listener);
    }
}
