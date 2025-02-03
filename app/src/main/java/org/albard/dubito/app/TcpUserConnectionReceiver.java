package org.albard.dubito.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;

public final class TcpUserConnectionReceiver implements UserConnectionReceiver {
    private final ServerSocket listeningSocket;
    private final Thread listeningThread;
    private final UserConnectionRepository userRepository;

    public TcpUserConnectionReceiver(UserConnectionRepository repository, ServerSocket socket) {
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
                if (!(userSocket.getRemoteSocketAddress() instanceof InetSocketAddress remoteEndPoint)
                        || !this.userRepository.addUser(remoteEndPoint)) {
                    // TODO: Log error
                    continue;
                }
                this.handleUserConnection(userSocket);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleUserConnection(final Socket userSocket) {
        Thread.ofVirtual().name("UserConnection-" + userSocket.getRemoteSocketAddress()).start(() -> {
            try {
                final InputStream stream = userSocket.getInputStream();
                final byte[] receiveBuffer = new byte[1024];
                while (this.isListening() && userSocket.isConnected()) {
                    final int readByteCount = stream.read(receiveBuffer);
                    if (readByteCount <= 0) {
                        break;
                    }
                    // TODO: Do work
                    Thread.sleep(Duration.ofMillis(10));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                this.userRepository.removeUser((InetSocketAddress) userSocket.getRemoteSocketAddress());
                try {
                    userSocket.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static TcpUserConnectionReceiver createAndBind(UserConnectionRepository repository, String bindAddress,
            int bindPort) throws UnknownHostException, IOException {
        return new TcpUserConnectionReceiver(repository,
                new ServerSocket(bindPort, 4, InetAddress.getByName(bindAddress)));
    }
}
