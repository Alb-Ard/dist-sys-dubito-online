package org.albard.dubito.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.connection.PeerConnectionReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.utils.Locked;
import org.albard.utils.Logger;
import org.albard.utils.ObservableHashMap;
import org.albard.utils.ObservableMap;
import org.albard.utils.ObservableMapListener;

public final class PeerNetworkImpl implements PeerNetwork {
    private final Lock connectionsLock = new ReentrantLock();
    private final Locked<ObservableMap<PeerId, PeerConnection>> connections = Locked
            .withExistingLock(new ObservableHashMap<>(), this.connectionsLock);
    private final PeerConnectionReceiver connectionReceiver;
    private final PeerIdExchanger peerIdExchanger;
    private final Set<MessageHandler> messageReceiverListeners = new HashSet<>();

    private BiConsumer<PeerId, PeerConnection> peerConnectedListener;
    private Consumer<PeerId> peerDisconnectedListener;

    private PeerNetworkImpl(final PeerId localPeerId, final PeerConnectionReceiver receiver) {
        this.peerIdExchanger = new PeerIdExchanger(localPeerId);
        this.connectionReceiver = receiver;
        this.connectionReceiver.setPeerConnectedListener(c -> {
            try {
                if (this.containsPeerAtEndPoint(c.getRemoteEndPoint())) {
                    return;
                }
                System.out.println(localPeerId + ": Received connection from " + c.getRemoteEndPoint());
                this.peerIdExchanger.exchangeIds(c).ifPresent(remotePeerId -> this.addConnection(remotePeerId, c));
            } catch (final Exception ex) {
                System.err.println(ex.getMessage());
                try {
                    c.close();
                } catch (final IOException ex) {
                    Logger.logError(this.getLocalPeerId() + ": Could not close connection: " + ex.getMessage());
                }
            }
        });
        this.connections.getValue().addListener(new ObservableMapListener<PeerId, PeerConnection>() {
            @Override
            public void entryAdded(final PeerId key, final PeerConnection value) {
                if (PeerNetworkImpl.this.peerConnectedListener != null) {
                    PeerNetworkImpl.this.peerConnectedListener.accept(key, value);
                }
            }

            @Override
            public void entryRemoved(final PeerId key, final PeerConnection value) {
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
        return Map.copyOf(this.connections.getValue());
    }

    @Override
    public int getPeerCount() {
        return this.connections.getValue().size();
    }

    @Override
    public boolean connectToPeer(final PeerEndPoint peerEndPoint) {
        System.out.println(this.getLocalPeerId() + ": Connecting to " + peerEndPoint);
        if (this.containsPeerAtEndPoint(peerEndPoint)) {
            return true;
        }
        if (!this.connectionsLock.tryLock()) {
            return false;
        }
        try {
            PeerConnection connection = null;
            try {
                final PeerConnection createdConnection = PeerConnection.createAndConnect("0.0.0.0", 0,
                        peerEndPoint.getHost(), peerEndPoint.getPort(), this.connectionReceiver.getMessengerFactory());
                connection = createdConnection;
                return this.peerIdExchanger.exchangeIds(connection)
                        .map(remotePeerId -> this.addConnection(remotePeerId, createdConnection)).orElse(false);
            } catch (final Exception ex) {
                System.err.println(ex.getMessage());
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (final Exception ex2) {
                    Logger.logError(ex2.getMessage());
                }
                return false;
            }
        } finally {
            this.connectionsLock.unlock();
        }
    }

    @Override
    public boolean disconnectFromPeer(final PeerId peerId) {
        if (!this.connectionsLock.tryLock()) {
            return false;
        }
        try {
            final PeerConnection connection = this.connections.getValue().remove(peerId);
            if (connection == null) {
                Logger.logError(this.getLocalPeerId() + ": Disconnecting from " + peerId + ", but peer was not found!");
                return false;
            }
            Logger.logInfo(this.getLocalPeerId() + ": Disconnecting from " + peerId);
            try {
                connection.close();
            } catch (final IOException ex) {
                Logger.logError(ex.getMessage());
            }
            return true;
        } finally {
            this.connectionsLock.unlock();
        }
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
    public PeerEndPoint getBindEndPoint() {
        return this.connectionReceiver.getBindEndPoint();
    }

    @Override
    public void close() {
        Logger.logInfo(this.getLocalPeerId() + ": Closing...");
        final Set<PeerId> connectedPeers = Set.copyOf(this.connections.getValue().keySet());
        for (final PeerId connectionId : connectedPeers) {
            this.disconnectFromPeer(connectionId);
        }
        try {
            this.connectionReceiver.close();
        } catch (final IOException ex) {
            Logger.logError(ex.getMessage());
        }
    }

    @Override
    public void sendMessage(final GameMessage message) {
        final Set<PeerConnection> receipients = getMessageActualReceipients(message);
        System.out.println(
                this.getLocalPeerId() + ": Sending " + message.getClass().getSimpleName() + " to " + receipients);
        receipients.forEach(e -> e.sendMessage(message));
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
    public PeerId getLocalPeerId() {
        return this.peerIdExchanger.getLocalPeerId();
    }

    private boolean addConnection(final PeerId id, final PeerConnection connection) {
        if (!this.connectionsLock.tryLock()) {
            return false;
        }
        try {
            System.out.println(this.getLocalPeerId() + ": Adding connection " + id);
            if (id == null) {
                Logger.logError(this.getLocalPeerId() + ": Provided Id for connection " + connection.getRemoteEndPoint()
                        + " is null (maybe Id exchange failed...)");
                try {
                    connection.close();
                } catch (final IOException ex) {
                }
                return false;
            }
            final PeerConnection oldConnection = this.connections.getValue().putIfAbsent(id, connection);
            if (oldConnection != null) {
                Logger.logInfo(this.getLocalPeerId() + ": A connection with the same PeerId is already present " + id);
                try {
                    connection.close();
                } catch (final IOException ex) {
                }
                return false;
            }
            Logger.logInfo(this.getLocalPeerId() + ": Connected to peer " + id);
            connection.addMessageListener(this::onMessageReceived);
            connection.addClosedListener(() -> this.disconnectFromPeer(id));
            return true;
        } finally {
            this.connectionsLock.unlock();
        }
    }

    private boolean containsPeerAtEndPoint(final PeerEndPoint peerEndPoint) {
        return this.connections.getValue().values().stream().map(c -> c.getRemoteEndPoint())
                .anyMatch(peerEndPoint::equals);
    }

    private Set<PeerConnection> getMessageActualReceipients(final GameMessage message) {
        final Set<PeerId> messageReceipients = message.getReceipients();
        if (messageReceipients == null) {
            return Set.copyOf(this.getPeers().values());
        }
        return this.getPeers().entrySet().stream().filter(e -> messageReceipients.contains(e.getKey()))
                .map(e -> e.getValue()).collect(Collectors.toSet());
    }

    private boolean onMessageReceived(final GameMessage message) {
        return this.messageReceiverListeners.stream().map(l -> l.handleMessage(message)).anyMatch(r -> r);
    }
}
