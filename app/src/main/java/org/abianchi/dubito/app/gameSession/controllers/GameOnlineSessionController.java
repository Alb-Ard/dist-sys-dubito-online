package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.CardValue;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.Player;
import org.abianchi.dubito.messages.CallLiarMessage;
import org.abianchi.dubito.messages.CardsThrownMessage;
import org.abianchi.dubito.messages.NewHandDrawnMessage;
import org.abianchi.dubito.messages.RoundCardGeneratedMessage;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;

import java.util.ArrayList;
import java.util.List;

public class GameOnlineSessionController<X extends OnlinePlayer> extends GameSessionController<X>
        implements MessageHandler {
    private final PeerNetwork sessionNetwork;
    private final boolean isOwner;
    private final Runnable onChanged;
    private final OnlinePlayer localPlayer;

    public GameOnlineSessionController(List<X> players, PeerNetwork network,
            boolean isOwner, Runnable onChanged) {
        super(players);
        this.sessionNetwork = network;
        this.isOwner = isOwner;
        this.onChanged = onChanged;
        this.localPlayer = players.stream().filter(el -> el.getOnlineId().equals(network.getLocalPeerId())).findFirst()
                .get();
        this.sessionNetwork.addMessageListener(this::handleMessage);
    }

    @Override
    public boolean handleMessage(GameMessage message) {
        // sender di messaggio avvisa che ha pescato una nuova mano
        if (message instanceof NewHandDrawnMessage handDrawnMessage) {
            System.out.println("Player " + handDrawnMessage.getSender() + " has drawn a new hand: "
                    + handDrawnMessage.getNewHand());
            Player player = this.getPlayerById(message.getSender());
            player.receiveNewHand(handDrawnMessage.getNewHand().stream().map(Card::ofType)
                    .toList());
            // devo qua segnalare il refresh della view (senza avere un riferimento alla
            // view)
            // devo rendere osservable questo Controller (GameOnlineSessionController) un
            // observable
            // uso Runnable
            this.onChanged.run();
            return true;
        }
        // sender ha lanciato le carte
        if (message instanceof CardsThrownMessage cardsThrownMessage) {
            System.out.println("Player " + cardsThrownMessage.getSender() + " has throw cards: "
                    + cardsThrownMessage.getThrownCards());
            cardsThrownMessage.getThrownCards().stream().map(Card::ofType)
                    .forEach(this::selectCard);
            this.playCards();
            this.onChanged.run();
            return true;
        }
        if (message instanceof CallLiarMessage) {
            System.out.println("Player " + message.getSender() + " has called liar");
            this.callLiar();
            this.onChanged.run();
            return true;
        }
        if (message instanceof RoundCardGeneratedMessage roundCardGeneratedMessage) {
            System.out.println("Player " + roundCardGeneratedMessage.getSender() + " has set the round card to "
                    + roundCardGeneratedMessage.getRoundCard());
            this.getCurrentGameState().setRoundCardType(roundCardGeneratedMessage.getRoundCard());
            this.onChanged.run();
            return true;
        }
        return false;
    }

    // devo fare override di newRound per inviare i messaggi (invio a tutti il
    // messaggio che questo utente ha una nuova mano di carte
    @Override
    public void newRound() {
        super.newRound(); // fa tutti i cambiamenti locali
        // se sono owner, setto la carta del round
        if (this.canGenerateRoundCard()) {
            final CardValue roundCardValue = this.getCurrentGameState().getRoundCardValue();
            System.out.println("Setting round card to " + roundCardValue);
            this.sessionNetwork.sendMessage(new RoundCardGeneratedMessage(sessionNetwork.getLocalPeerId(), null,
                    roundCardValue));
        }
        final var newHand = this.localPlayer.getHand().stream().map(e -> e.getCardType())
                .toList();
        System.out.println("Sending my new hand: " + newHand);
        sessionNetwork.sendMessage(new NewHandDrawnMessage(sessionNetwork.getLocalPeerId(), null,
                newHand));
    }

    @Override
    public boolean isActivePlayer(final int index) {
        return super.isActivePlayer(index) && index >= 0 && this.getPlayers().get(index).equals(this.localPlayer);
    }
    @Override
    protected boolean canGenerateRoundCard() {
        return this.isOwner;
    }

    @Override
    protected void giveNewHand() {
        // When new hands are given, I only want to generate a hand for my local player
        List<Card> newHand = new ArrayList<>();
        for (int i = 0; i < Player.MAX_HAND_SIZE; i++) {
            newHand.add(Card.random());
        }
        this.localPlayer.receiveNewHand(newHand);
    }

    @Override
    public void playCards() {
        // gestisco prima il messaggio per indicare che ho giocato le carte (in modo che
        // l'elenco di carte selezionate non venga resettato), per poi fare localmente
        // il resto
        if (this.isCurrentPlayerLocal()) {
            sessionNetwork.sendMessage(new CardsThrownMessage(sessionNetwork.getLocalPeerId(), null,
                    this.getSelectedCards().stream().map(e -> e.getCardType())
                            .toList()));
        }
        super.playCards();
    }

    @Override
    public void callLiar() {
        // gestione messaggio callLiar come con playCard
        if (this.isCurrentPlayerLocal()) {
            sessionNetwork.sendMessage(new CallLiarMessage(sessionNetwork.getLocalPeerId(), null));
        }
        super.callLiar();
    }

    // con questo prendiamo il primo player della sessione con lo specifico Id
    // passato
    private X getPlayerById(PeerId id) {
        return this.getPlayers().stream()
                .filter(el -> el.getOnlineId().equals(id)).findFirst().get();
    }

    private boolean isCurrentPlayerLocal() {
        return this.getCurrentPlayer().map(this.localPlayer::equals)
                .orElse(false);
    }
}
