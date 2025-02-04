package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;

import org.albard.dubito.app.UserConnectionRepository;

public final class TcpUserConnectionReceiver implements UserConnectionReceiver {
    private final ServerSocket listeningSocket;
    private final Thread listeningThread;
    private final UserConnectionRepository<Socket> userRepository;

    public TcpUserConnectionReceiver(UserConnectionRepository<Socket> repository, ServerSocket socket) {
        this.userRepository = repository;
        this.listeningSocket = socket;
        this.listeningThread = new Thread(this::listenForUsers, "UserReceiver");
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
        } finally {
            this.userRepository.clear();
        }
    }

    @Override
    public int getUserCount() {
        return this.userRepository.getUserCount();
    }

    private void listenForUsers() {
        while (this.isListening()) {
            try {
                final Socket userSocket = this.listeningSocket.accept();
                this.handleUserConnection(userSocket);
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleUserConnection(final Socket userSocket) {
        Thread.ofVirtual().name("UserConnection-" + userSocket.getRemoteSocketAddress()).start(() -> {
            try {
                if (this.userRepository.addUser((InetSocketAddress) userSocket.getRemoteSocketAddress(), userSocket)) {
                    while (this.isListening() && !userSocket.isClosed() && userSocket.isConnected()) {
                        Thread.sleep(Duration.ofMillis(10));
                    }
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
            try {
                this.userRepository.removeUser((InetSocketAddress) userSocket.getRemoteSocketAddress());
                userSocket.close();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static TcpUserConnectionReceiver createAndBind(UserConnectionRepository<Socket> repository,
            String bindAddress, int bindPort) throws UnknownHostException, IOException {
        return new TcpUserConnectionReceiver(repository,
                new ServerSocket(bindPort, 4, InetAddress.getByName(bindAddress)));
    }
}
