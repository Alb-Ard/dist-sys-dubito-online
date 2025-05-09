package org.albard.dubito.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.handlers.RouteMessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.ConnectionRouteMessage;

public final class PeerStarNetwork implements PeerNetwork {
    private final PeerNetwork baseNetwork;

    private BiConsumer<PeerId, PeerConnection> peerConnectedlistener;

    private PeerStarNetwork(final PeerId localPeerId, final PeerNetwork baseNetwork) {
        this.baseNetwork = baseNetwork;
        this.baseNetwork.setPeerConnectedListener((id, connection) -> {
            try {
                System.out.println("Peer connected to ID: " + id);
                if (PeerStarNetwork.this.peerConnectedlistener != null) {
                    PeerStarNetwork.this.peerConnectedlistener.accept(id, connection);
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
        this.addMessageListener(new RouteMessageHandler(this::propagatePeer, this::disconnectFromPeer));
    }

    public static PeerNetwork createBound(final PeerId localPeerId, final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws IOException {
        return new PeerStarNetwork(localPeerId,
                PeerNetwork.createBound(localPeerId, bindAddress, bindPort, messengerFactory));
    }

    @Override
    public void close() throws IOException {
        this.baseNetwork.close();
    }

    @Override
    public Map<PeerId, PeerConnection> getPeers() {
        return this.baseNetwork.getPeers();
    }

    @Override
    public int getPeerCount() {
        return this.baseNetwork.getPeerCount();
    }

    @Override
    public boolean connectToPeer(final PeerEndPoint peerEndPoint) {
        if(this.baseNetwork.connectToPeer(peerEndPoint)) {
            Map.Entry<PeerId,PeerConnection> newPeer = this.getPeers().entrySet().stream().filter(el -> el.getValue().getRemoteEndPoint().equals(peerEndPoint))
                    .findFirst().get();
            this.sendMessage(new ConnectionRouteMessage(this.getLocalPeerId(), Set.of(newPeer.getKey()), this.getBindEndPoint()));
            return true;
        }
        return false;
    }

    @Override
    public PeerEndPoint getBindEndPoint() {
        return this.baseNetwork.getBindEndPoint();
    }

    @Override
    public void setPeerConnectedListener(final BiConsumer<PeerId, PeerConnection> listener) {
        this.peerConnectedlistener = listener;
    }

    @Override
    public void setPeerDisconnectedListener(final Consumer<PeerId> listener) {
        this.baseNetwork.setPeerDisconnectedListener(listener);
    }

    @Override
    public boolean disconnectFromPeer(final PeerId peerId) {
        return this.baseNetwork.disconnectFromPeer(peerId);
    }

    @Override
    public void sendMessage(final GameMessage message) {
        this.baseNetwork.sendMessage(message);
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.baseNetwork.addMessageListener(listener);
    }

    @Override
    public void removeMessageListener(final MessageHandler listener) {
        this.baseNetwork.removeMessageListener(listener);
    }

    @Override
    public PeerId getLocalPeerId() {
        return this.baseNetwork.getLocalPeerId();
    }

    private void propagatePeer(final PeerEndPoint peerEndPoint) {
        Optional<Map.Entry<PeerId,PeerConnection>> existingPeer = this.getPeers().entrySet().stream().filter(el -> el.getValue().getRemoteEndPoint().equals(peerEndPoint))
                .findFirst(); // verifico se esiste già questa connessione
        if(existingPeer.isPresent()) {
            /*
            per fare sì che questo Peer si colleghi al resto, recupero tutti quelli connessi a me, rimuovo dalla lista
            me stesso e il nuovo Peer e invio un messaggio per connettere il nuovo Peer a tutti gli altri
            (ovviamente non mando un nuovo messaggio di connessione a me stesso o al Peer nuovo stesso)
             */
            Set<PeerId> receipients = new HashSet<>(PeerStarNetwork.this.getPeers().keySet());
            receipients.remove(this.getLocalPeerId());
            receipients.remove(existingPeer.get().getKey());
            System.out.println(this.getLocalPeerId() + ": Propagating connection " + existingPeer.get().getKey() + " to " + receipients + " IP:" + peerEndPoint);
            this.sendMessage(new ConnectionRouteMessage(this.getLocalPeerId(), receipients, peerEndPoint));
            return;
        }
        if(this.baseNetwork.connectToPeer(peerEndPoint)) {
            Map.Entry<PeerId,PeerConnection> newPeer = this.getPeers().entrySet().stream().filter(el -> el.getValue().getRemoteEndPoint().equals(peerEndPoint))
                    .findFirst().get();
        }
    }
}
