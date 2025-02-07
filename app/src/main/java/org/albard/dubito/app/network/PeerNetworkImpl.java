package org.albard.dubito.app.network;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

import org.albard.dubito.app.ObservableHashMap;
import org.albard.dubito.app.ObservableMap;
import org.albard.dubito.app.ObservableMapListener;
import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.connection.PeerConnectionReceiver;

public final class PeerNetworkImpl implements PeerNetwork {
    private final ObservableMap<PeerId, PeerConnection> connections = new ObservableHashMap<>();
    private final PeerConnectionReceiver connectionReceiver;
    private final PeerIdExchanger peerIdExchanger;

    private BiConsumer<PeerId, PeerConnection> peerConnectedListener;

    private PeerNetworkImpl(final PeerId localPeerId, final PeerConnectionReceiver receiver) {
        this.connectionReceiver = receiver;
        this.connectionReceiver.setPeerConnectedListener(c -> {
            try {
                this.bindToIdAndAddConnection(c);
            } catch (final Exception ex) {
                ex.printStackTrace();
                try {
                    c.close();
                } catch (final Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });
        this.peerIdExchanger = new PeerIdExchanger(localPeerId);
        this.connections.addListener(new ObservableMapListener<PeerId, PeerConnection>() {
            @Override
            public void entryAdded(final PeerId key, final PeerConnection value) {
                if (PeerNetworkImpl.this.peerConnectedListener != null) {
                    PeerNetworkImpl.this.peerConnectedListener.accept(key, value);
                }
            }

            @Override
            public void entryRemoved(PeerId key, PeerConnection value) {
            }
        });
    }

    public static PeerNetworkImpl createBound(final PeerId localPeerId, final String bindAddress, final int bindPort)
            throws IOException {
        return new PeerNetworkImpl(localPeerId, PeerConnectionReceiver.createBound(bindAddress, bindPort));
    }

    @Override
    public void start() {
        try {
            this.connectionReceiver.start();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Map<PeerId, PeerConnection> getPeers() {
        return Map.copyOf(this.connections);
    }

    @Override
    public int getPeerCount() {
        return this.connections.size();
    }

    @Override
    public boolean connectToPeer(final PeerEndPoint peerEndPoint) {
        if (this.containsPeerAtEndPoint(peerEndPoint)) {
            return true;
        }
        PeerConnection connection = null;
        try {
            connection = PeerConnection.createAndConnect("0.0.0.0", 0, peerEndPoint.getHost(), peerEndPoint.getPort());
            this.bindToIdAndAddConnection(connection);
        } catch (final Exception ex) {
            ex.printStackTrace();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (final Exception ex2) {
                ex2.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean bindRemotePeer(final PeerId peerId, final PeerEndPoint peerEndPoint) {
        if (this.connections.containsKey(peerId) || this.containsPeerAtEndPoint(peerEndPoint)) {
            return false;
        }
        PeerConnection connection = null;
        try {
            connection = PeerConnection.createAndConnect("0.0.0.0", 0, peerEndPoint.getHost(), peerEndPoint.getPort());
            final PeerId remotePeerId = this.peerIdExchanger.exchangeIds(connection);
            if (remotePeerId != peerId) {
                throw new Exception("Given bound peer Id and exchanged peer Id do not match");
            }
            this.connections.putIfAbsent(remotePeerId, connection);
        } catch (final Exception ex) {
            ex.printStackTrace();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (final Exception ex2) {
                ex2.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @Override
    public void setPeerConnectedListener(final BiConsumer<PeerId, PeerConnection> listener) {
        this.peerConnectedListener = listener;
    }

    @Override
    public void close() {
        for (final PeerConnection connection : this.connections.values()) {
            try {
                connection.close();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
        try {
            this.connectionReceiver.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private PeerId bindToIdAndAddConnection(final PeerConnection connection) throws IOException {
        final PeerId remotePeerId = this.peerIdExchanger.exchangeIds(connection);
        this.connections.putIfAbsent(remotePeerId, connection);
        return remotePeerId;
    }

    private boolean containsPeerAtEndPoint(final PeerEndPoint peerEndPoint) {
        return this.connections.values().stream().map(c -> c.getSocket().getRemoteSocketAddress())
                .map(PeerEndPoint::createFromAddress).anyMatch(peerEndPoint::equals);
    }
}
