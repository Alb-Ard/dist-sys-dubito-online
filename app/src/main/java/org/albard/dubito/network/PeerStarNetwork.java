package org.albard.dubito.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
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

    private PeerStarNetwork(final PeerNetwork baseNetwork) {
        this.baseNetwork = baseNetwork;
        this.baseNetwork.setPeerConnectedListener((id, connection) -> {
            try {
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
        return new PeerStarNetwork(PeerNetwork.createBound(localPeerId, bindAddress, bindPort, messengerFactory));
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
        if (!this.baseNetwork.connectToPeer(peerEndPoint)) {
            return false;
        }
        return this.getPeers().entrySet().stream().filter(el -> el.getValue().getRemoteEndPoint().equals(peerEndPoint))
                .findFirst().map(newPeer -> {
                    System.out.println(this.getLocalPeerId() + ": Sending my server endpoint " + this.getBindEndPoint()
                            + " to " + newPeer.getKey()
                            + " at " + peerEndPoint);
                    this.sendMessage(new ConnectionRouteMessage(this.getLocalPeerId(), Set.of(newPeer.getKey()),
                            this.getBindEndPoint()));
                    return true;
                }).orElse(false);
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

    private void propagatePeer(final PeerId peerId, final PeerEndPoint peerEndPoint) {
        System.out.println(this.getLocalPeerId() + ": Received peer " + peerId + " route " + peerEndPoint);
        this.getPeers().entrySet().stream().filter(e -> e.getKey().equals(peerId))
                .findFirst().ifPresentOrElse(e -> {
                    final Set<PeerId> receipients = new HashSet<>(this.getPeers().keySet());
                    receipients.remove(this.getLocalPeerId());
                    receipients.remove(peerId);
                    System.out.println(this.getLocalPeerId() + ": Propagating connection " + peerId
                            + " at " + peerEndPoint + " to " + receipients);
                    this.sendMessage(new ConnectionRouteMessage(peerId, receipients, peerEndPoint));
                }, () -> {
                    System.out.println(this.getLocalPeerId() + ": Connecting to propagated connection " + peerEndPoint);
                    // This failing might not be an error:
                    // Since we are propagating connections between hosts, and connections
                    // can't be uniquely identified without receiving their id, when a duplicated
                    // id is received it could just be that we are connecting to an host that
                    // is already connected to me but with a different EndPoint.
                    this.baseNetwork.connectToPeer(peerEndPoint);
                });
    }
}
