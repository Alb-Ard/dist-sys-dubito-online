package org.albard.dubito.app.views;

import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.abianchi.dubito.app.gameSession.views.GameButton;
import org.albard.dubito.lobby.models.LobbyDisplay;
import org.albard.mvc.ExecutableViewCommand;
import org.albard.mvc.ViewCommand;

public final class LobbyListItemView extends JPanel {
    private final ExecutableViewCommand<Consumer<LobbyDisplay>> lobbySelectedCommand = new ExecutableViewCommand<>();

    public LobbyListItemView(final LobbyDisplay lobby) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(new JLabel(createLobbyDescription(lobby)));
        final JButton joinButton = new GameButton("Join >");
        joinButton.addActionListener(e -> this.lobbySelectedCommand.execute(l -> l.accept(lobby)));
        this.add(joinButton);
    }

    public ViewCommand<Consumer<LobbyDisplay>> getLobbySelectedCommand() {
        return this.lobbySelectedCommand;
    }

    private static String createLobbyDescription(final LobbyDisplay lobby) {
        final StringBuilder builder = new StringBuilder(lobby.name()).append(" ")
                .append(lobby.currentParticipantCount()).append("/").append(lobby.maxParticipantCount());
        if (lobby.isPasswordProtected()) {
            builder.append(" P");
        }
        return builder.toString();
    }
}