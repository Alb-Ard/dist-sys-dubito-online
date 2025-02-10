package org.albard.dubito.app.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.messaging.MessageDispatcher;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.handlers.RouteMessageHandler;
import org.albard.dubito.app.messaging.messages.RouteMessage;

public final class PeerStarNetwork implements PeerNetwork {
    private final PeerNetwork baseNetwork;

    private BiConsumer<PeerId, PeerConnection> peerConnectedlistener;

    private PeerStarNetwork(final PeerId localPeerId, final PeerNetwork baseNetwork, final MessageDispatcher dispatcher,
            final MessengerFactory messengerFactory) {
        this.baseNetwork = baseNetwork;
        this.baseNetwork.setPeerConnectedListener((id, connection) -> {
            try {
                Set<PeerId> receipients = new HashSet<>(PeerStarNetwork.this.getPeers().keySet());
                receipients.remove(localPeerId);
                receipients.remove(id);
                dispatcher.addPeer(id, messengerFactory.createSender(connection.getSocket()),
                        messengerFactory.createReceiver(connection.getSocket()));
                System.out.println(localPeerId + ": Propagating connection " + id);
                dispatcher.sendMessage(new RouteMessage(id, receipients,
                        PeerEndPoint.createFromAddress(connection.getSocket().getRemoteSocketAddress())));
                if (PeerStarNetwork.this.peerConnectedlistener != null) {
                    PeerStarNetwork.this.peerConnectedlistener.accept(id, connection);
                }
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        });
        dispatcher.addMessageListener(new RouteMessageHandler(this::bindRemotePeer));
    }

    public static PeerNetwork createBound(final PeerId localPeerId, final String bindAddress, final int bindPort,
            final MessageDispatcher dispatcher, final MessengerFactory messengerFactory) throws IOException {
        return new PeerStarNetwork(localPeerId, PeerNetwork.createBound(localPeerId, bindAddress, bindPort), dispatcher,
                messengerFactory);
    }

    @Override
    public void close() throws IOException {
        this.baseNetwork.close();
    }

    @Override
    public void start() {
        this.baseNetwork.start();
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
    public boolean connectToPeer(PeerEndPoint peerEndPoint) {
        return this.baseNetwork.connectToPeer(peerEndPoint);
    }

    @Override
    public boolean bindRemotePeer(PeerId peerId, PeerEndPoint peerEndPoint) {
        System.out.println("Binding peer " + peerId);
        return this.baseNetwork.bindRemotePeer(peerId, peerEndPoint);
    }

    @Override
    public void setPeerConnectedListener(BiConsumer<PeerId, PeerConnection> listener) {
        this.peerConnectedlistener = listener;
    }
}
