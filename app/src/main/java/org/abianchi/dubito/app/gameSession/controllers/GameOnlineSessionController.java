package org.abianchi.dubito.app.gameSession.controllers;

import org.abianchi.dubito.app.gameSession.models.OnlinePlayer;
import org.abianchi.dubito.app.gameSession.models.OnlinePlayerImpl;
import org.abianchi.dubito.app.gameSession.models.Player;
import org.abianchi.dubito.messages.CallLiarMessage;
import org.abianchi.dubito.messages.CardsThrownMessage;
import org.abianchi.dubito.messages.NewHandDrawnMessage;
import org.abianchi.dubito.messages.RoundCardGeneratedMessage;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;

import java.util.List;
import java.util.stream.Collectors;

public class GameOnlineSessionController extends GameSessionController implements MessageHandler {

    private final PeerNetwork sessionNetwork;
    private final boolean isOwner;
    private final Runnable onChanged;
    public GameOnlineSessionController(List<OnlinePlayer> players, PeerNetwork network,
               boolean isOwner, Runnable onChanged) {
        super(players.stream().map(el -> (Player)el).collect(Collectors.toList()),
                players.stream().filter(el -> el.getOnlineId().equals(network.getLocalPeerId()))
                .map(el -> (Player)el).findFirst());
        this.sessionNetwork = network;
        this.isOwner = isOwner;
        this.onChanged = onChanged;
        this.sessionNetwork.addMessageListener(this::handleMessage);
    }


    @Override
    public boolean handleMessage(GameMessage message) {
        // sender di messaggio avvisa che ha pescato una nuova mano
        if(message instanceof NewHandDrawnMessage handDrawnMessage) {
            Player player = this.getPlayerById(message.getSender());
            player.receiveNewHand(handDrawnMessage.getNewHand());
            // devo qua segnalare il refresh della view (senza avere un riferimento alla view)
            // devo rendere osservable questo Controller (GameOnlineSessionController) un observable
            // uso Runnable
            this.onChanged.run();
            return true;
        }
        // sender ha lanciato le carte
        if(message instanceof CardsThrownMessage cardsThrownMessage) {
            cardsThrownMessage.getThrownCards().forEach(this::selectCard);
            this.playCards();
            this.onChanged.run();
            return true;
        }
        if(message instanceof CallLiarMessage callLiarMessage) {
            this.callLiar();
            this.onChanged.run();
            return true;
        }
        if(message instanceof RoundCardGeneratedMessage roundCardGeneratedMessage) {
            this.getCurrentGameState().setRoundCardType(roundCardGeneratedMessage.getRoundCard());
            this.onChanged.run();
            return true;
        }
        return false;
    }

    // devo fare override di newRound per inviare i messaggi (invio a tutti il messaggio che questo utente ha una nuova mano di carte
    @Override
    public void newRound() {
        super.newRound(); // fa tutti i cambiamenti locali
        // se sono owner, setto la carta del round
        if(this.canGenerateRoundCard()) {
            this.sessionNetwork.sendMessage(new RoundCardGeneratedMessage(sessionNetwork.getLocalPeerId(), null,
                    this.getCurrentGameState().getRoundCardValue()));
        }
        sessionNetwork.sendMessage(new NewHandDrawnMessage(sessionNetwork.getLocalPeerId(), null,
                this.getPlayerById(sessionNetwork.getLocalPeerId()).getHand()));
    }

    @Override
    protected boolean canGenerateRoundCard() {
        return this.isOwner;
    }

    @Override
    public void playCards() {
        // gestisco prima il messaggio per indicare che ho giocato le carte, per poi fare localmente il resto
        OnlinePlayer currentPlayer = (OnlinePlayer)this.getCurrentPlayer();
        if(currentPlayer.getOnlineId().equals(sessionNetwork.getLocalPeerId())) {
            sessionNetwork.sendMessage(new CardsThrownMessage(sessionNetwork.getLocalPeerId(), null,
                    this.getSelectedCards()));
        }
        super.playCards();
    }

    @Override
    public void callLiar() {
        // gestione messaggio callLiar come con playCard
        OnlinePlayer currentPlayer = (OnlinePlayer)this.getCurrentPlayer();
        if(currentPlayer.getOnlineId().equals(sessionNetwork.getLocalPeerId())) {
            sessionNetwork.sendMessage(new CallLiarMessage(sessionNetwork.getLocalPeerId(), null));
        }
        super.callLiar();
    }

    // con questo prendiamo il primo player della sessione con lo specifico Id passato
    private OnlinePlayer getPlayerById(PeerId id) {
        return this.getPlayers().stream().map(p -> (OnlinePlayer)p)
                .filter(el -> el.getOnlineId().equals(id)).findFirst().get();
    }
}
