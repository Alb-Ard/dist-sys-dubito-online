package org.albard.dubito.app.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

import org.albard.dubito.app.connection.PeerConnection;

public interface PeerNetwork extends Closeable {
    static PeerNetwork createBound(final PeerId localPeerId, final String bindAddress, final int bindPort)
            throws IOException {
        return PeerNetworkImpl.createBound(localPeerId, bindAddress, bindPort);
    }

    void start();

    Map<PeerId, PeerConnection> getPeers();

    int getPeerCount();

    boolean connectToPeer(PeerEndPoint peerEndPoint);

    boolean bindRemotePeer(PeerId peerId, PeerEndPoint peerEndPoint);

    void setPeerConnectedListener(final BiConsumer<PeerId, PeerConnection> listener);
}
