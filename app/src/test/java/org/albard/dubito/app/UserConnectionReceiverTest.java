package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;

import org.albard.dubito.app.connection.UserConnectionReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionReceiverTest {
    @Test
    void testBindsCorrectly() {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        Assertions.assertDoesNotThrow(() -> UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000).close());
    }

    @Test
    void testBindsIdle() throws UnknownHostException, IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1",
                9000)) {
            Assertions.assertFalse(receiver.isListening());
            Assertions.assertEquals(0, receiver.getUserCount());
        }
    }

    @Test
    void testStartReceiving() throws UnknownHostException, IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1",
                9000)) {
            Assertions.assertDoesNotThrow(() -> receiver.start());
            Assertions.assertTrue(receiver.isListening());
        }
    }

    @Test
    void testStartAgain() throws IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1",
                9000)) {
            receiver.start();
            Assertions.assertThrows(Exception.class, () -> receiver.start());
        }
    }

    @Test
    void testCloseWhenNotStarted() throws UnknownHostException, IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        UserConnectionReceiver receiver = null;
        try {
            receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000);
            receiver.close();
        } finally {
            Assertions.assertFalse(receiver.isListening());
            Assertions.assertEquals(0, receiver.getUserCount());
        }
    }

    @Test
    void testCloses() throws IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        UserConnectionReceiver receiver = null;
        try {
            receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000);
            receiver.start();
            receiver.close();
        } finally {
            Assertions.assertFalse(receiver.isListening());
            Assertions.assertEquals(0, receiver.getUserCount());
        }
    }

    @Test
    void testCloseAgain() throws IOException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        UserConnectionReceiver receiver = null;
        try {
            receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000);
            receiver.start();
            receiver.close();
            Assertions.assertDoesNotThrow(receiver::close);
        } finally {

        }
    }

    @Test
    void testConnectClient() throws IOException, InterruptedException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1", 9000);
                final Socket userSocket = new Socket()) {
            receiver.start();
            Assertions.assertDoesNotThrow(() -> userSocket.connect(new InetSocketAddress("127.0.0.1", 9000)));
            // I can't be sure that the receiver processed the connection immediatly, I give
            // it some time to finish accepting the user...
            Thread.sleep(Duration.ofMillis(250));
            Assertions.assertEquals(1, receiver.getUserCount());
        }
    }

    @Test
    void testDisconnectClient() throws IOException, InterruptedException {
        final UserConnectionRepository<Socket> repository = UserConnectionRepository.createEmpty();
        repository.addUserListener(new UserRepositoryListener<Socket>() {
            @Override
            public void handleUserAdded(InetSocketAddress endPoint, Socket connection) {
                try {
                    // Fake reading to detect when the remote end closes the connection
                    if (connection.getInputStream().read() < 0) {
                        connection.close();
                    }
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void handleUserRemoved(InetSocketAddress endPoint, Socket connection) {
            }
        });
        try (final UserConnectionReceiver receiver = UserConnectionReceiver.createBound(repository, "127.0.0.1",
                9000)) {
            final Socket userSocket = new Socket();
            receiver.start();
            userSocket.connect(new InetSocketAddress("127.0.0.1", 9000));
            // Wait for receiver to finish accepting the user
            Thread.sleep(Duration.ofMillis(250));
            userSocket.close();
            // Wait for receiver to finish disconnecting the user
            Thread.sleep(Duration.ofMillis(250));
            Assertions.assertEquals(0, receiver.getUserCount());
        }
    }
}
