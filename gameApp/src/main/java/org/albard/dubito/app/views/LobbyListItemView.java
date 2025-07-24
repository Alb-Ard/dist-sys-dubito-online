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
    private final JLabel descriptionLabel;
    private final ExecutableViewCommand<Consumer<LobbyDisplay>> lobbySelectedCommand = new ExecutableViewCommand<>();

    private LobbyDisplay currentInfo;

    public LobbyListItemView(final LobbyDisplay lobby) {
        this.currentInfo = lobby;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.descriptionLabel = new JLabel();
        final JButton joinButton = new GameButton("Join >");
        this.add(descriptionLabel);
        this.add(joinButton);
        joinButton.addActionListener(e -> this.lobbySelectedCommand.execute(l -> l.accept(this.currentInfo)));
        this.updateInfo(lobby);
    }

    public ViewCommand<Consumer<LobbyDisplay>> getLobbySelectedCommand() {
        return this.lobbySelectedCommand;
    }

    public void updateInfo(final LobbyDisplay info) {
        this.currentInfo = info;
        this.descriptionLabel.setText(createLobbyDescription(info));
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