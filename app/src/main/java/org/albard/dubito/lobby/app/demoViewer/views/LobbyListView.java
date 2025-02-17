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

import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.dubito.lobby.models.LobbyId;

public final class LobbyListView extends JPanel {
    private final JLabel noLobbiesLabel = new JLabel("No lobbies found.");

    private final Set<Runnable> createLobbyListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<LobbyId>> lobbySelectedListeners = Collections.synchronizedSet(new HashSet<>());

    private final DefaultListModel<LobbyDisplay> model;

    public LobbyListView(final DefaultListModel<LobbyDisplay> model) {
        this.model = model;
        this.setLayout(new BorderLayout(4, 4));
        this.add(this.noLobbiesLabel, BorderLayout.NORTH);
        final JButton createLobbyButton = new JButton("Create Lobby");
        createLobbyButton.addActionListener(e -> this.createLobbyListeners.forEach(l -> l.run()));
        this.add(createLobbyButton, BorderLayout.SOUTH);
        final JPanel lobbyList = new JPanel();
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
                LobbyListView.this.noLobbiesLabel.setVisible(model.getSize() <= 0);
                lobbyList.removeAll();
                for (int i = 0; i < model.getSize(); i++) {
                    final LobbyListItemView item = new LobbyListItemView();
                    item.setLobby(model.get(i));
                    item.addLobbySelectedListener(
                            x -> LobbyListView.this.lobbySelectedListeners.forEach(l -> l.accept(x)));
                    lobbyList.add(item);
                }
            }
        });
        this.add(lobbyList, BorderLayout.CENTER);
    }

    public void addLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.add(listener);
    }

    public void removeLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.remove(listener);
    }

    public void addCreateLobbyListener(final Runnable listener) {
        this.createLobbyListeners.add(listener);
    }

    public void removeCreateLobbyListener(final Runnable listener) {
        this.createLobbyListeners.remove(listener);
    }
}
