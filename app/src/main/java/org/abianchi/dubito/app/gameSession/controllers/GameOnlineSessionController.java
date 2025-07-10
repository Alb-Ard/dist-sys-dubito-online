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
import org.albard.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class GameOnlineSessionController<X extends OnlinePlayer> extends GameSessionController<X>
        implements MessageHandler {
    private final PeerNetwork sessionNetwork;
    private final Runnable onChanged;
    private final OnlinePlayer localPlayer;

    private int ownerPeerIndex;

    public GameOnlineSessionController(final List<X> players, final PeerNetwork network, final int ownerPeerIndex,
            final Runnable onChanged) {
        super(players);
        this.sessionNetwork = network;
        this.ownerPeerIndex = ownerPeerIndex;
        this.onChanged = onChanged;
        this.localPlayer = players.stream().filter(el -> el.getOnlineId().equals(network.getLocalPeerId())).findFirst()
                .get();
        this.sessionNetwork.addMessageListener(this);
        this.sessionNetwork.addPeerDisconnectedListener(this::setPeerDisconnectedListener);
    }

    @Override
    public boolean handleMessage(GameMessage message) {
        // sender di messaggio avvisa che ha pescato una nuova mano
        if (message instanceof NewHandDrawnMessage handDrawnMessage) {
            Logger.logInfo("Player " + handDrawnMessage.getSender() + " has drawn a new hand: "
                    + handDrawnMessage.getNewHand());
            Player player = this.getPlayerById(message.getSender());
            player.receiveNewHand(handDrawnMessage.getNewHand().stream().map(Card::ofType).toList());
            // devo qua segnalare il refresh della view (senza avere un riferimento alla
            // view) devo rendere osservable questo Controller (GameOnlineSessionController)
            // un observable uso Runnable
            this.onChanged.run();
            return true;
        }
        // sender ha lanciato le carte
        if (message instanceof CardsThrownMessage cardsThrownMessage) {
            Logger.logInfo("Player " + cardsThrownMessage.getSender() + " has throw cards: "
                    + cardsThrownMessage.getThrownCards());
            final List<Card> playedCards = cardsThrownMessage.getThrownCards().stream().map(Card::ofType).toList();
            this.playCards(playedCards);
            this.onChanged.run();
            return true;
        }
        if (message instanceof CallLiarMessage) {
            Logger.logInfo("Player " + message.getSender() + " has called liar");
            this.callLiar();
            this.onChanged.run();
            return true;
        }
        if (message instanceof RoundCardGeneratedMessage roundCardGeneratedMessage) {
            Logger.logInfo("Player " + roundCardGeneratedMessage.getSender() + " has set the round card to "
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
        if (this.canGenerateRoundCard() && this.getCurrentGameState().getRoundCardValue().isPresent()) {
            final CardValue roundCardValue = this.getCurrentGameState().getRoundCardValue().get();
            Logger.logInfo("Setting round card to " + roundCardValue);
            this.sessionNetwork
                    .sendMessage(new RoundCardGeneratedMessage(sessionNetwork.getLocalPeerId(), null, roundCardValue));
        }
        final var newHand = this.localPlayer.getHand().stream().map(e -> e.getCardType()).toList();
        Logger.logInfo("Sending my new hand: " + newHand);
        sessionNetwork.sendMessage(new NewHandDrawnMessage(sessionNetwork.getLocalPeerId(), null, newHand));
        this.onChanged.run();
    }

    @Override
    public boolean isActivePlayer(final int index) {
        return super.isActivePlayer(index) && index >= 0
                && this.getSessionPlayers().get(index).equals(this.localPlayer);
    }

    @Override
    protected boolean canGenerateRoundCard() {
        return this.isOwner();
    }

    private void setPeerDisconnectedListener(final PeerId peerId) {
        /* viene chiamato su tutti, non devo scambiare messaggi */
        Logger.logInfo(this.localPlayer.getOnlineId() + ": Removing player " + peerId);
        this.removePlayer(this.getSessionPlayers().indexOf(this.getPlayerById(peerId)));
        // If the owner peer has left, pass the ownership to the next valid player
        if (this.getOwnerPeer().equals(this.getPlayerById(peerId))) {
            final List<X> players = this.getSessionPlayers();
            final int oldOwnerIndex = this.ownerPeerIndex;
            for (int i = 1; i < players.size(); i++) {
                if (players.get(i).getLives() > 0) {
                    this.ownerPeerIndex = i;
                    Logger.logInfo(this.localPlayer.getOnlineId() + ": Ownership passed to "
                            + this.getOwnerPeer().getOnlineId());
                    break;
                }
            }
            if (this.ownerPeerIndex == oldOwnerIndex) {
                Logger.logError(this.localPlayer.getOnlineId() + ": Could not transfer ownership!");
            }
        }
        this.onChanged.run();
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
    public void playCards(final List<Card> cards) {
        // gestisco prima il messaggio per indicare che ho giocato le carte (in modo che
        // l'elenco di carte selezionate non venga resettato), per poi fare localmente
        // il resto
        Logger.logInfo("Playing cards " + cards);
        if (this.isCurrentPlayerLocal()) {
            Logger.logInfo("Sending thrown cards...");
            CardsThrownMessage message = new CardsThrownMessage(sessionNetwork.getLocalPeerId(), null,
                    cards.stream().map(Card::getCardType).toList());
            Logger.logInfo("The message is " + message);
            sessionNetwork.sendMessage(message);
        }
        super.playCards(cards);
        this.onChanged.run();
    }

    @Override
    public void callLiar() {
        // gestione messaggio callLiar come con playCard
        if (this.isCurrentPlayerLocal()) {
            CallLiarMessage message = new CallLiarMessage(sessionNetwork.getLocalPeerId(), null);
            Logger.logInfo("The message is " + message);
            sessionNetwork.sendMessage(message);
        }
        super.callLiar();
        this.onChanged.run();
    }

    // con questo prendiamo il primo player della sessione con lo specifico Id
    // passato
    private X getPlayerById(PeerId id) {
        return this.getSessionPlayers().stream().filter(el -> el.getOnlineId().equals(id)).findFirst().get();
    }

    private boolean isCurrentPlayerLocal() {
        return this.getCurrentPlayer().map(this.localPlayer::equals).orElse(false);
    }

    private boolean isOwner() {
        return this.getOwnerPeer().equals(this.localPlayer);
    }

    private X getOwnerPeer() {
        return this.getSessionPlayers().get(this.ownerPeerIndex);
    }
}
