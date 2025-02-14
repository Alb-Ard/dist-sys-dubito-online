package org.albard.dubito.app.lobby.app.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyId;

public final class SwingLobbyListView extends JPanel implements LobbyListView {
    private final static class LobbyDisplayView extends JPanel {
        public LobbyDisplayView(final LobbyDisplay lobby, final Consumer<LobbyId> lobbySelectedListener) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(new JLabel(lobby.name()));
            final JButton joinButton = new JButton("Join >");
            joinButton.addActionListener(e -> lobbySelectedListener.accept(lobby.id()));
            this.add(joinButton);
        }
    }

    private final Set<Consumer<LobbyId>> lobbySelectedListeners = Collections.synchronizedSet(new HashSet<>());

    public SwingLobbyListView() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void setLobbies(final List<LobbyDisplay> lobbies) {
        this.removeAll();
        lobbies.stream().sorted((a, b) -> a.name().compareTo(b.name()))
                .map(l -> new LobbyDisplayView(l, this::invokeLobbySelectedListeners)).forEach(this::add);
    }

    @Override
    public void addLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.add(listener);
    }

    @Override
    public void removeLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.remove(listener);
    }

    private void invokeLobbySelectedListeners(final LobbyId selectedLobbyId) {
        this.lobbySelectedListeners.forEach(l -> l.accept(selectedLobbyId));
    }
}
