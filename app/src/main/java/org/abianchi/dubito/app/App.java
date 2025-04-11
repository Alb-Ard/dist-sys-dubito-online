package org.abianchi.dubito.app;

import org.abianchi.dubito.app.gameSession.controllers.GameSessionController;
import org.abianchi.dubito.app.gameSession.models.Player;
import org.abianchi.dubito.app.gameSession.models.PlayerImpl;
import org.abianchi.dubito.app.gameSession.views.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final int MAXPLAYERS = 4;

    public static void main(final String[] args) {

        final List<Player> players  = new ArrayList<>();
        for(int i = 0; i < MAXPLAYERS; i++) {
            players.add(new PlayerImpl());
        }
        final GameSessionController controller = new GameSessionController(players);
        controller.newRound();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameBoardView(controller);
            }
        });

    }
}
