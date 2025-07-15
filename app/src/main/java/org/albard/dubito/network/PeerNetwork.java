package org.albard.dubito.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.messaging.Messenger;
import org.albard.dubito.messaging.MessengerFactory;

public interface PeerNetwork extends Closeable, Messenger {
    @FunctionalInterface
    public interface PeerConnectedListener {
        void peerConnected(PeerId id, PeerConnection connection, PeerEndPoint remoteEndPoint);
    }

    static PeerNetwork createBound(final PeerId localPeerId, final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws IOException {
        return PeerNetworkImpl.createBound(localPeerId, bindAddress, bindPort, messengerFactory);
    }

    PeerId getLocalPeerId();

    Map<PeerId, PeerConnection> getPeers();

    int getPeerCount();

    boolean connectToPeer(PeerEndPoint peerEndPoint);

    boolean disconnectFromPeer(PeerId peerId);

    void addPeerConnectedListener(final PeerConnectedListener listener);

    void removePeerConnectedListener(final PeerConnectedListener listener);

    void addPeerDisconnectedListener(final Consumer<PeerId> listener);

    void removePeerDisconnectedListener(final Consumer<PeerId> listener);

    PeerEndPoint getBindEndPoint();
}
