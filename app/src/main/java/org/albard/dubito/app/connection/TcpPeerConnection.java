package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class TcpPeerConnection implements PeerConnection {
    private final Socket socket;
    private final MessageReceiver messageReceiver;
    private final MessageSender messageSender;

    private TcpPeerConnection(final Socket socket, final MessengerFactory messengerFactory) throws IOException {
        this.socket = socket;
        this.messageSender = messengerFactory.createSender(socket);
        this.messageReceiver = messengerFactory.createReceiver(socket);
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
        socket.close();
    }

    @Override
    public void sendMessage(final GameMessage message) {
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
    public void addClosedListener(final ReceiverClosedListener listener) {
        this.messageReceiver.addClosedListener(listener);

    }

    @Override
    public void removeClosedListener(final ReceiverClosedListener listener) {
        this.messageReceiver.removeClosedListener(listener);
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }
}
