package org.albard.dubito.app.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.handlers.RouteMessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.RouteAddedMessage;

public final class PeerStarNetwork implements PeerNetwork {
    private final PeerNetwork baseNetwork;

    private BiConsumer<PeerId, PeerConnection> peerConnectedlistener;

    private PeerStarNetwork(final PeerId localPeerId, final PeerNetwork baseNetwork) {
        this.baseNetwork = baseNetwork;
        this.baseNetwork.setPeerConnectedListener((id, connection) -> {
            try {
                Set<PeerId> receipients = new HashSet<>(PeerStarNetwork.this.getPeers().keySet());
                receipients.remove(localPeerId);
                receipients.remove(id);
                System.out.println(localPeerId + ": Propagating connection " + id + " to " + receipients);
                this.sendMessage(new RouteAddedMessage(localPeerId, receipients, connection.getRemoteEndPoint()));
                if (PeerStarNetwork.this.peerConnectedlistener != null) {
                    PeerStarNetwork.this.peerConnectedlistener.accept(id, connection);
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
        this.addMessageListener(new RouteMessageHandler(this::connectToPeer, this::disconnectFromPeer));
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
        return this.baseNetwork.connectToPeer(peerEndPoint);
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
}
