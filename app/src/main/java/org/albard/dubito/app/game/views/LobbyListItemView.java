package org.albard.dubito.app.game.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.albard.dubito.lobby.models.LobbyDisplay;

public final class LobbyListItemView extends JPanel {
    private final JLabel nameLabel = new JLabel();
    private final Set<Consumer<LobbyDisplay>> lobbySelectedListeners = Collections.synchronizedSet(new HashSet<>());

    private Optional<LobbyDisplay> lobby = Optional.empty();

    public LobbyListItemView() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(this.nameLabel);
        final JButton joinButton = new JButton("Join >");
        joinButton.addActionListener(e -> lobby.ifPresent(m -> this.lobbySelectedListeners.forEach(l -> l.accept(m))));
        this.add(joinButton);
    }

    public void setLobby(final LobbyDisplay lobby) {
        this.lobby = Optional.ofNullable(lobby);
        this.lobby.ifPresentOrElse(l -> {
            this.nameLabel.setText(createLobbyDescription(lobby));
        }, () -> {
            this.nameLabel.setText("[Unknown]");
        });
    }

    public void addLobbySelectedListener(final Consumer<LobbyDisplay> listener) {
        this.lobbySelectedListeners.add(listener);
    }

    public void removeLobbySelectedListener(final Consumer<LobbyDisplay> listener) {
        this.lobbySelectedListeners.remove(listener);
    }

    private static String createLobbyDescription(final LobbyDisplay lobby) {
        final StringBuilder builder = new StringBuilder().append(lobby.name()).append(" ")
                .append(lobby.currentParticipantCount()).append("/").append(lobby.maxParticipantCount());
        if (lobby.isPasswordProtected()) {
            builder.append(" P");
        }
        return builder.toString();
    }
}