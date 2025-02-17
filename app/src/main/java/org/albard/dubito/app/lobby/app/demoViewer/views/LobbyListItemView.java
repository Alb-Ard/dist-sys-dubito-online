package org.albard.dubito.app.lobby.app.demoViewer.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.albard.dubito.app.lobby.models.LobbyDisplay;
import org.albard.dubito.app.lobby.models.LobbyId;

public final class LobbyListItemView extends JPanel {
    private final JLabel nameLabel = new JLabel();
    private final Set<Consumer<LobbyId>> lobbySelectedListeners = Collections.synchronizedSet(new HashSet<>());

    private Optional<LobbyDisplay> lobby = Optional.empty();

    public LobbyListItemView() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(this.nameLabel);
        final JButton joinButton = new JButton("Join >");
        joinButton.addActionListener(
                e -> lobby.ifPresent(m -> this.lobbySelectedListeners.forEach(l -> l.accept(m.id()))));
        this.add(joinButton);
    }

    public void setLobby(final LobbyDisplay lobby) {
        this.lobby = Optional.ofNullable(lobby);
        this.lobby.ifPresentOrElse(l -> this.nameLabel.setText(l.name()), () -> this.nameLabel.setText("[Unknown]"));
    }

    public void addLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.add(listener);
    }

    public void removeLobbySelectedListener(final Consumer<LobbyId> listener) {
        this.lobbySelectedListeners.remove(listener);
    }
}