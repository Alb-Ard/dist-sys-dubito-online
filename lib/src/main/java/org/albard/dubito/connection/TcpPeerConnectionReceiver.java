package org.albard.dubito.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.utils.Logger;

public final class TcpPeerConnectionReceiver implements PeerConnectionReceiver {
    private static final int SERVER_BACKLOG_SIZE = 4;

    private final ServerSocket listeningSocket;
    private final Thread listeningThread;
    private final MessengerFactory messengerFactory;
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();

    private Consumer<PeerConnection> peerConnectedListener;

    private TcpPeerConnectionReceiver(final ServerSocket socket, final MessengerFactory messengerFactory) {
        this.listeningSocket = socket;
        this.listeningThread = new Thread(this::listenForUsers, "UserReceiver");
        this.messengerFactory = messengerFactory;
    }

    public static TcpPeerConnectionReceiver createBound(final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws UnknownHostException, IOException {
        final TcpPeerConnectionReceiver receiver = new TcpPeerConnectionReceiver(
                new ServerSocket(bindPort, SERVER_BACKLOG_SIZE, InetAddress.getByName(bindAddress)), messengerFactory);
        receiver.start();
        return receiver;
    }

    @Override
    public boolean isListening() {
        return this.listeningThread.isAlive() && !this.listeningSocket.isClosed();
    }

    @Override
    public void close() {
        try {
            this.listeningSocket.close();
            this.listeningThread.join();
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessengerFactory getMessengerFactory() {
        return this.messengerFactory;
    }

    @Override
    public PeerEndPoint getBindEndPoint() {
        return PeerEndPoint.ofAddress(this.listeningSocket.getLocalSocketAddress());
    }

    private void start() throws IOException {
        this.listeningThread.start();
    }

    private void listenForUsers() {
        while (this.isListening()) {
            try {
                final Socket userSocket = this.listeningSocket.accept();
                this.listenerExecutor.submit(() -> {
                    try {
                        this.peerConnectedListener
                                .accept(TcpPeerConnection.createConnected(userSocket, this.messengerFactory));
                    } catch (final Exception ex) {
                    }
                });
            } catch (final IOException ex) {
                Logger.logError(ex.getMessage());
            }
        }
    }

    @Override
    public void setPeerConnectedListener(final Consumer<PeerConnection> listener) {
        this.peerConnectedListener = listener;
    }
}
