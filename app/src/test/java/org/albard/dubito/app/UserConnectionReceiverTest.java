package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.connection.PeerConnectionReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionReceiverTest {
    @Test
    void testBindsCorrectly() {
        Assertions.assertDoesNotThrow(() -> PeerConnectionReceiver.createBound("127.0.0.1", 9000).close());
    }

    @Test
    void testBindsIdle() throws UnknownHostException, IOException {
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000)) {
            Assertions.assertFalse(receiver.isListening());
        }
    }

    @Test
    void testStartReceiving() throws UnknownHostException, IOException {
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000)) {
            Assertions.assertDoesNotThrow(() -> receiver.start());
            Assertions.assertTrue(receiver.isListening());
        }
    }

    @Test
    void testStartAgain() throws IOException {
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000)) {
            receiver.start();
            Assertions.assertThrows(Exception.class, () -> receiver.start());
        }
    }

    @Test
    void testCloseWhenNotStarted() throws UnknownHostException, IOException {
        PeerConnectionReceiver receiver = null;
        try {
            receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000);
            receiver.close();
        } finally {
            Assertions.assertFalse(receiver.isListening());
        }
    }

    @Test
    void testCloses() throws IOException {
        PeerConnectionReceiver receiver = null;
        try {
            receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000);
            receiver.start();
            receiver.close();
        } finally {
            Assertions.assertFalse(receiver.isListening());
        }
    }

    @Test
    void testCloseAgain() throws IOException {
        PeerConnectionReceiver receiver = null;
        try {
            receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000);
            receiver.start();
            receiver.close();
            Assertions.assertDoesNotThrow(receiver::close);
        } finally {

        }
    }

    @Test
    void testConnectClient() throws IOException, InterruptedException {
        final List<PeerConnection> connections = new ArrayList<>();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000);
                final Socket userSocket = new Socket()) {
            receiver.setPeerConnectedListener(connections::add);
            receiver.start();
            Assertions.assertDoesNotThrow(() -> userSocket.connect(new InetSocketAddress("127.0.0.1", 9000)));
            // I can't be sure that the receiver processed the connection immediatly, I give
            // it some time to finish accepting the user...
            Thread.sleep(Duration.ofMillis(250));
            Assertions.assertEquals(1, connections.size());
        }
    }
}
