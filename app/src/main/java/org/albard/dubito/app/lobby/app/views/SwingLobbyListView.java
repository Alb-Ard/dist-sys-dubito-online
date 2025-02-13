package org.albard.dubito.app.lobby.app.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyId;

public final class SwingLobbyListView extends JPanel implements LobbyListView {
    private final static class LobbyDisplayView extends JPanel {
        public LobbyDisplayView(final LobbyDisplay lobby) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(new JLabel(lobby.name()));
        }
    }

    private final Set<Consumer<LobbyId>> lobbySelectedListeners = Collections.synchronizedSet(new HashSet<>());
    private final JList<LobbyDisplay> list = new JList<>();

    public SwingLobbyListView() {
        this.list.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new LobbyDisplayView(value));
        this.list.addListSelectionListener(e -> this.invokeLobbySelectedListener(this.list.getSelectedValue().id()));
        this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(this.list);
    }

    @Override
    public void setLobbies(final List<LobbyDisplay> lobbies) {
        final DefaultListModel<LobbyDisplay> model = new DefaultListModel<>();
        lobbies.stream().sorted((a, b) -> a.name().compareTo(b.name())).forEach(v -> {
            model.addElement(v);
        });
        this.list.setModel(model);

    }

    @Override
    public void addLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.add(listener);
    }

    @Override
    public void removeLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.remove(listener);
    }

    private void invokeLobbySelectedListener(final LobbyId selectedLobbyId) {
        this.lobbySelectedListeners.forEach(l -> l.accept(selectedLobbyId));
    }
}
