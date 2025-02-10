package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.albard.dubito.app.messaging.MessengerFactory;

public final class TcpPeerConnectionReceiver implements PeerConnectionReceiver {
    private static final int SERVER_BACKLOG_SIZE = 4;

    private final ServerSocket listeningSocket;
    private final Thread listeningThread;
    private final MessengerFactory messengerFactory;

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

    private void start() throws IOException {
        this.listeningThread.start();
    }

    private void listenForUsers() {
        while (this.isListening()) {
            try {
                final Socket userSocket = this.listeningSocket.accept();
                try {
                    this.peerConnectedListener
                            .accept(TcpPeerConnection.createConnected(userSocket, this.messengerFactory));
                } catch (final Exception ex) {
                }
            } catch (final SocketException ex) {
                System.err.println(ex.getMessage());
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void setPeerConnectedListener(final Consumer<PeerConnection> listener) {
        this.peerConnectedListener = listener;
    }
}
