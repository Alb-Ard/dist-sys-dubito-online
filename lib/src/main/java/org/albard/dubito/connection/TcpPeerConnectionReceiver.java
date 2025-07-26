package org.albard.dubito.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.utils.Locked;
import org.albard.utils.Logger;

public final class TcpPeerConnectionReceiver implements PeerConnectionReceiver {
    private final ServerSocket listeningSocket;
    private final Thread listeningThread;
    private final MessengerFactory messengerFactory;
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private final Locked<List<PeerConnection>> peerConnections = Locked.of(new ArrayList<>());

    private Consumer<PeerConnection> peerConnectedListener;
    private Consumer<PeerConnection> peerDisconnectedListener;

    private TcpPeerConnectionReceiver(final ServerSocket socket, final MessengerFactory messengerFactory) {
        this.listeningSocket = socket;
        this.listeningThread = new Thread(this::listenForUsers, "UserReceiver");
        this.messengerFactory = messengerFactory;
    }

    public static TcpPeerConnectionReceiver createBound(final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws UnknownHostException, IOException {
        final ServerSocket socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(bindAddress, bindPort));
        final TcpPeerConnectionReceiver receiver = new TcpPeerConnectionReceiver(socket, messengerFactory);
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
            this.peerConnections.exchange(x -> {
                for (final PeerConnection peerConnection : x) {
                    this.closeConnection(peerConnection);
                }
                x.clear();
                return x;
            });
            this.listeningSocket.close();
            this.listeningThread.join();
        } catch (final Exception ex) {
            Logger.logError(ex.getMessage());
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

    @Override
    public void setPeerConnectedListener(final Consumer<PeerConnection> listener) {
        this.peerConnectedListener = listener;
    }

    @Override
    public void setPeerDisconnectedListener(final Consumer<PeerConnection> listener) {
        this.peerDisconnectedListener = listener;
    }

    private void start() throws IOException {
        this.listeningThread.start();
    }

    private void listenForUsers() {
        while (this.isListening()) {
            try {
                final Socket userSocket = this.listeningSocket.accept();
                this.listenerExecutor.submit(() -> {
                    final TcpPeerConnection[] connection = new TcpPeerConnection[] { null };
                    this.peerConnections.exchange(connections -> {
                        try {
                            connection[0] = TcpPeerConnection.createConnected(userSocket, this.messengerFactory);
                            connection[0].addClosedListener(() -> this.closeConnection(connection[0]));
                            connections.add(connection[0]);
                            this.peerConnectedListener.accept(connection[0]);
                        } catch (final Exception ex) {
                            if (connection[0] != null) {
                                this.closeConnection(connection[0]);
                            }
                        }
                        return connections;
                    });
                });
            } catch (final Exception ex) {
                Logger.logError(ex.getMessage());
            }
        }
    }

    private void closeConnection(final PeerConnection peerConnection) {
        this.peerConnections.exchange(x -> {
            try {
                peerConnection.close();
            } catch (final Exception ex) {
            }
            x.remove(peerConnection);
            return x;
        });
        this.peerDisconnectedListener.accept(peerConnection);
    }
}
