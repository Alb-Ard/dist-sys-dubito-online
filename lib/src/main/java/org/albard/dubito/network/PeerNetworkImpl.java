package org.albard.dubito.network;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.connection.PeerConnectionReceiver;
import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.utils.Listeners;
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
    private final PeerExchanger peerIdExchanger;
    private final Set<MessageHandler> messageHandlers = Collections.synchronizedSet(new HashSet<>());
    private final Set<MessageHandler> messageHandlersToRemove = Collections.synchronizedSet(new HashSet<>());
    private final Set<PeerConnectedListener> peerConnectedListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<PeerId>> peerDisconnectedListeners = Collections.synchronizedSet(new HashSet<>());
    // Used to keep track of connections that are being established but are not
    // complete
    private final Set<PeerEndPoint> pendingConnectionEndPoints = Collections.synchronizedSet(new HashSet<>());

    private PeerNetworkImpl(final PeerId localPeerId, final PeerConnectionReceiver receiver) {
        this.peerIdExchanger = new PeerExchanger(localPeerId, receiver.getBindEndPoint());
        this.connectionReceiver = receiver;
        this.connectionReceiver.setPeerConnectedListener(c -> {
            final boolean handleResult = this.peerIdExchanger.exchangeIds(c).map(connectionData -> {
                return this.runLocked("peerConnectedListener", () -> {
                    // If the remote peer send "0.0.0.0" as it's server IP, then override it with
                    // the IP he has connected with
                    final PeerEndPoint serverEndPoint = connectionData.getValue().getHost().equals("0.0.0.0")
                            ? PeerEndPoint.ofValues(c.getRemoteEndPoint().getHost(),
                                    connectionData.getValue().getPort())
                            : connectionData.getValue();
                    if (this.containsPeerAtEndPoint(c.getRemoteEndPoint())
                            || this.containsPeerAtEndPoint(serverEndPoint)
                            || !this.pendingConnectionEndPoints.add(serverEndPoint)) {
                        Logger.logInfo(this.getLocalPeerId() + ": Connection to " + c.getRemoteEndPoint()
                                + " is already being established, skipping...");
                        return false;
                    }
                    Logger.logInfo(this.getLocalPeerId() + ": Completing connection to " + connectionData.getKey()
                            + " - " + c.getRemoteEndPoint() + " (and server " + serverEndPoint + ")");
                    if (!this.addConnection(connectionData.getKey(), c)) {
                        Logger.logError(this.getLocalPeerId() + ": Connection to " + c.getRemoteEndPoint()
                                + " FAILED (Could not add connection)");
                        return false;
                    }
                    this.pendingConnectionEndPoints.remove(serverEndPoint);
                    this.onPeerConnected(connectionData.getKey(), c, serverEndPoint);
                    return true;
                }, () -> false);
            }).orElse(false);
            if (!handleResult) {
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
                Logger.logInfo(localPeerId + ": Added connection to peer " + key + " via " + value.getRemoteEndPoint());
            }

            @Override
            public void entryRemoved(final PeerId key, final PeerConnection value) {
                Logger.logInfo(
                        localPeerId + ": Removed connection to peer " + key + " via " + value.getRemoteEndPoint());
                Listeners.runAll(PeerNetworkImpl.this.peerDisconnectedListeners, key);
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
        return this.runLocked("connectToPeer", () -> {
            Logger.logInfo(this.getLocalPeerId() + ": Connecting to " + peerEndPoint);
            if (this.containsPeerAtEndPoint(peerEndPoint) || !this.pendingConnectionEndPoints.add(peerEndPoint)) {
                Logger.logInfo(this.getLocalPeerId() + ": Peer " + peerEndPoint + " already connected, skipping...");
                return true;
            }
            PeerConnection connection = null;
            try {
                final PeerConnection createdConnection = PeerConnection.createAndConnect("0.0.0.0", 0,
                        peerEndPoint.getHost(), peerEndPoint.getPort(), this.connectionReceiver.getMessengerFactory());
                connection = createdConnection;
                final var connectionData = this.peerIdExchanger.exchangeIds(connection);
                if (connectionData.isEmpty()) {
                    Logger.logError(
                            this.getLocalPeerId() + ": Connection to " + peerEndPoint + " FAILED (Ping not received)");
                    return false;
                }
                if (!this.addConnection(connectionData.get().getKey(), createdConnection)) {
                    return false;
                }
                this.onPeerConnected(connectionData.get().getKey(), createdConnection, connectionData.get().getValue());
                Logger.logInfo(this.getLocalPeerId() + ": Connection to " + peerEndPoint + " completed");
                return true;
            } catch (final Exception ex) {
                Logger.logError(this.getLocalPeerId() + ": Connection to " + peerEndPoint + " FAILED ("
                        + ex.getMessage() + ")");
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (final Exception ex2) {
                    Logger.logError(ex2.getMessage());
                }
                return false;
            } finally {
                this.pendingConnectionEndPoints.remove(peerEndPoint);
            }
        }, () -> false);
    }

    @Override
    public boolean disconnectFromPeer(final PeerId peerId) {
        return this.runLocked("disconnectFromPeer", () -> {
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
        }, () -> false);
    }

    @Override
    public void addPeerConnectedListener(final PeerConnectedListener listener) {
        this.peerConnectedListeners.add(listener);
    }

    @Override
    public void removePeerConnectedListener(final PeerConnectedListener listener) {
        this.peerConnectedListeners.remove(listener);
    }

    @Override
    public void addPeerDisconnectedListener(final Consumer<PeerId> listener) {
        this.peerDisconnectedListeners.add(listener);
    }

    @Override
    public void removePeerDisconnectedListener(final Consumer<PeerId> listener) {
        this.peerDisconnectedListeners.remove(listener);
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
            try {
                this.disconnectFromPeer(connectionId);
            } catch (final Exception ex) {
                Logger.logError(
                        this.getLocalPeerId() + ": Error disconnecting from " + connectionId + ": " + ex.getMessage());
            }
        }
        try {
            this.connectionReceiver.close();
        } catch (final Exception ex) {
            Logger.logError(this.getLocalPeerId() + ": Error closing receiver: " + ex.getMessage());
        }
    }

    @Override
    public void sendMessage(final GameMessage message) {
        final Set<PeerConnection> receipients = getMessageActualReceipients(message);
        Logger.logInfo(
                this.getLocalPeerId() + ": Sending " + message.getClass().getSimpleName() + " to " + receipients);
        receipients.forEach(e -> e.sendMessage(message));
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.messageHandlersToRemove.remove(listener);
        this.messageHandlers.add(listener);
    }

    @Override
    public void queueRemoveMessageListener(final MessageHandler listener) {
        this.messageHandlersToRemove.add(listener);
    }

    @Override
    public PeerId getLocalPeerId() {
        return this.peerIdExchanger.getLocalPeerId();
    }

    private void onPeerConnected(final PeerId id, final PeerConnection connection, final PeerEndPoint remoteEndPoint) {
        Set.copyOf(this.peerConnectedListeners).forEach(l -> l.peerConnected(id, connection, remoteEndPoint));
    }

    private boolean addConnection(final PeerId id, final PeerConnection connection) {
        return this.runLocked("addConnection", () -> {
            Logger.logInfo(this.getLocalPeerId() + ": Adding connection " + id);
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
        }, () -> false);
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
        return MessageReceiver.handleMessageAndUpdateHandlers(message, this.messageHandlers,
                this.messageHandlersToRemove);
    }

    private <X> X runLocked(final String caller, final Supplier<X> action, final Supplier<X> failedAction) {
        try {
            Logger.logDebug(this.getLocalPeerId() + ": " + caller + " ACQUIRING LOCK...");
            if (!this.connectionsLock.tryLock(1, TimeUnit.SECONDS)) {
                Logger.logError(this.getLocalPeerId() + ": " + caller + " ACQUIRE FAILED (Timeout)");
                return failedAction.get();
            }
        } catch (final InterruptedException ex) {
            Logger.logError(this.getLocalPeerId() + ": " + caller + " ACQUIRE  FAILED (" + ex.getMessage() + ")");
            return failedAction.get();
        }
        Logger.logDebug(this.getLocalPeerId() + ": " + caller + " LOCK ACQUIRED");
        try {
            return action.get();
        } catch (final Exception ex) {
            Logger.logError(this.getLocalPeerId() + ": " + caller + " ACTION FAILED (" + ex.getMessage() + ")");
            return failedAction.get();
        } finally {
            Logger.logDebug(this.getLocalPeerId() + ": " + caller + " LOCK RELEASED");
            this.connectionsLock.unlock();
        }
    }
}
