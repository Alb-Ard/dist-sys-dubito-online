package org.albard.dubito.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessageSender;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.utils.Logger;
import org.albard.utils.ObservableCloseable;

public final class TcpPeerConnection implements PeerConnection {
    private final Socket socket;
    private final MessageReceiver messageReceiver;
    private final MessageSender messageSender;
    private final Set<ClosedListener> closedListeners = new HashSet<>();

    private TcpPeerConnection(final Socket socket, final MessengerFactory messengerFactory) throws IOException {
        this.socket = socket;
        this.messageSender = messengerFactory.createSender(socket);
        this.messageReceiver = messengerFactory.createReceiver(socket);
        if (this.messageReceiver instanceof ObservableCloseable observableCloseable) {
            observableCloseable.addClosedListener(() -> {
                try {
                    this.close();
                } catch (final IOException ex) {
                    Logger.logError(ex.getMessage());
                }
            });
        }
    }

    public static TcpPeerConnection createConnected(final Socket socket, final MessengerFactory messengerFactory)
            throws IOException {
        if (!socket.isConnected()) {
            throw new SocketException("Socket is not connected");
        }
        return new TcpPeerConnection(socket, messengerFactory);
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
        this.closedListeners.forEach(l -> {
            try {
                l.closed();
            } catch (final Exception ex) {
            }
        });
    }

    @Override
    public void sendMessage(final GameMessage message) {
        Logger.logInfo(this + ": Sending message " + message.getClass().getSimpleName());
        this.messageSender.sendMessage(message);
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.messageReceiver.addMessageListener(listener);
    }

    @Override
    public void removeMessageListener(final MessageHandler listener) {
        this.messageReceiver.removeMessageListener(listener);
    }

    @Override
    public void addClosedListener(final ClosedListener listener) {
        this.closedListeners.add(listener);
    }

    @Override
    public void removeClosedListener(final ClosedListener listener) {
        this.closedListeners.remove(listener);
    }

    @Override
    public PeerEndPoint getRemoteEndPoint() {
        return PeerEndPoint.ofAddress(this.socket.getRemoteSocketAddress());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("TcpConnection[").append(this.socket.getLocalSocketAddress()).append(" -> ")
                .append(this.socket.getRemoteSocketAddress()).append("]").toString();
    }
}
