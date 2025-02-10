package org.albard.dubito.app.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.albard.dubito.app.ObservableHashMap;
import org.albard.dubito.app.ObservableMap;
import org.albard.dubito.app.ObservableMapListener;
import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.connection.PeerConnectionReceiver;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class PeerNetworkImpl implements PeerNetwork {
    private final ObservableMap<PeerId, PeerConnection> connections = new ObservableHashMap<>();
    private final PeerConnectionReceiver connectionReceiver;
    private final PeerIdExchanger peerIdExchanger;
    private final Set<MessageHandler> messageReceiverListeners = new HashSet<>();
    private final Set<ReceiverClosedListener> closedListeners = new HashSet<>();

    private BiConsumer<PeerId, PeerConnection> peerConnectedListener;
    private Consumer<PeerId> peerDisconnectedListener;

    private PeerNetworkImpl(final PeerId localPeerId, final PeerConnectionReceiver receiver) {
        this.peerIdExchanger = new PeerIdExchanger(localPeerId);
        this.connectionReceiver = receiver;
        this.connectionReceiver.setPeerConnectedListener(c -> {
            try {
                System.out
                        .println(localPeerId + ": Received connection from " + c.getSocket().getRemoteSocketAddress());
                final PeerId remotePeerId = this.peerIdExchanger.exchangeIds(c);
                this.addConnection(remotePeerId, c);
            } catch (final Exception ex) {
                ex.printStackTrace();
                try {
                    c.close();
                } catch (final Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });
        this.connections.addListener(new ObservableMapListener<PeerId, PeerConnection>() {
            @Override
            public void entryAdded(final PeerId key, final PeerConnection value) {
                if (PeerNetworkImpl.this.peerConnectedListener != null) {
                    PeerNetworkImpl.this.peerConnectedListener.accept(key, value);
                }
            }

            @Override
            public void entryRemoved(PeerId key, PeerConnection value) {
                if (PeerNetworkImpl.this.peerDisconnectedListener != null) {
                    PeerNetworkImpl.this.peerDisconnectedListener.accept(key);
                }
            }
        });
    }

    public static PeerNetworkImpl createBound(final PeerId localPeerId, final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws IOException {
        return new PeerNetworkImpl(localPeerId,
                PeerConnectionReceiver.createBound(bindAddress, bindPort, messengerFactory));
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
            connection = PeerConnection.createAndConnect("0.0.0.0", 0, peerEndPoint.getHost(), peerEndPoint.getPort(),
                    this.connectionReceiver.getMessengerFactory());
            final PeerId remotePeerId = this.peerIdExchanger.exchangeIds(connection);
            return this.addConnection(remotePeerId, connection);
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
    }

    @Override
    public boolean disconnectFromPeer(final PeerId peerId) {
        final PeerConnection connection = this.connections.remove(peerId);
        if (connection == null) {
            return false;
        }
        try {
            connection.close();
        } catch (final IOException ex) {
            System.err.println(ex.getMessage());
        }
        return true;
    }

    @Override
    public void setPeerConnectedListener(final BiConsumer<PeerId, PeerConnection> listener) {
        this.peerConnectedListener = listener;
    }

    @Override
    public void setPeerDisconnectedListener(final Consumer<PeerId> listener) {
        this.peerDisconnectedListener = listener;
    }

    @Override
    public void close() {
        final Set<PeerId> connectedPeers = Set.copyOf(this.connections.keySet());
        for (final PeerId connectionId : connectedPeers) {
            this.disconnectFromPeer(connectionId);
        }
        try {
            this.connectionReceiver.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        this.closedListeners.forEach(l -> l.receiverClosed());
    }

    @Override
    public void sendMessage(final GameMessage message) {
        this.getPeers().entrySet().stream().filter(e -> message.getReceipients().contains(e.getKey()))
                .forEach(e -> e.getValue().sendMessage(message));
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.messageReceiverListeners.add(listener);
    }

    @Override
    public void removeMessageListener(final MessageHandler listener) {
        this.messageReceiverListeners.remove(listener);
    }

    @Override
    public void addClosedListener(final ReceiverClosedListener listener) {
        this.closedListeners.add(listener);
    }

    @Override
    public void removeClosedListener(final ReceiverClosedListener listener) {
        this.closedListeners.remove(listener);
    }

    private boolean addConnection(final PeerId id, final PeerConnection connection) {
        if (id == null) {
            System.err.println("Provided Id is null (maybe Id exchange failed...)");
            try {
                connection.close();
            } catch (final IOException ex) {
            }
            return false;
        }
        final PeerConnection oldConnection = this.connections.putIfAbsent(id, connection);
        if (oldConnection != null) {
            System.err.println("A connection with the same PeerId is already present");
            try {
                connection.close();
            } catch (final IOException ex) {
            }
            return false;
        }
        connection.addMessageListener(this::onMessageReceived);
        connection.addClosedListener(() -> this.disconnectFromPeer(id));
        return true;
    }

    private boolean containsPeerAtEndPoint(final PeerEndPoint peerEndPoint) {
        return this.connections.values().stream().map(c -> c.getSocket().getRemoteSocketAddress())
                .map(PeerEndPoint::createFromAddress).anyMatch(peerEndPoint::equals);
    }

    private boolean onMessageReceived(final GameMessage message) {
        return this.messageReceiverListeners.stream().map(l -> l.handleMessage(message)).anyMatch(r -> r);
    }
}
