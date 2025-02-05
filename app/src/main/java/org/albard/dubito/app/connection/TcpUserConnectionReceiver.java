package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public final class TcpUserConnectionReceiver implements UserConnectionReceiver {
    private static final int SERVER_BACKLOG_SIZE = 4;

    private final ServerSocket listeningSocket;
    private final Thread listeningThread;

    private Consumer<UserConnection> userConnectedListener;

    private TcpUserConnectionReceiver(final ServerSocket socket) {
        this.listeningSocket = socket;
        this.listeningThread = new Thread(this::listenForUsers, "UserReceiver");
    }

    public static TcpUserConnectionReceiver createBound(final String bindAddress, final int bindPort)
            throws UnknownHostException, IOException {
        return new TcpUserConnectionReceiver(
                new ServerSocket(bindPort, SERVER_BACKLOG_SIZE, InetAddress.getByName(bindAddress)));
    }

    @Override
    public void start() throws IOException {
        this.listeningThread.start();
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

    private void listenForUsers() {
        while (this.isListening()) {
            try {
                final Socket userSocket = this.listeningSocket.accept();
                try {
                    this.userConnectedListener.accept(TcpUserConnection.createConnected(userSocket));
                } catch (final Exception ex) {
                }
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void setUserConnectedListener(final Consumer<UserConnection> listener) {
        this.userConnectedListener = listener;
    }
}
