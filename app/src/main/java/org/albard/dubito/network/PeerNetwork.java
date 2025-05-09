package org.albard.dubito.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.messaging.Messenger;
import org.albard.dubito.messaging.MessengerFactory;

public interface PeerNetwork extends Closeable, Messenger {
    static PeerNetwork createBound(final PeerId localPeerId, final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws IOException {
        return PeerNetworkImpl.createBound(localPeerId, bindAddress, bindPort, messengerFactory);
    }

    PeerId getLocalPeerId();

    Map<PeerId, PeerConnection> getPeers();

    int getPeerCount();

    boolean connectToPeer(PeerEndPoint peerEndPoint);

    boolean disconnectFromPeer(PeerId peerId);

    void setPeerConnectedListener(final BiConsumer<PeerId, PeerConnection> listener);

    void setPeerDisconnectedListener(final Consumer<PeerId> listener);

    PeerEndPoint getBindEndPoint();
}
