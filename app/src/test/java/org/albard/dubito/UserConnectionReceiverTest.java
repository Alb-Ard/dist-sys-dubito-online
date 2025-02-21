package org.albard.dubito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.connection.PeerConnectionReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionReceiverTest {
    @Test
    void testBindsCorrectly() {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        Assertions.assertDoesNotThrow(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, messengerFactory).close());
    }

    @Test
    void testStartReceiving() throws UnknownHostException, IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000,
                messengerFactory)) {
            Assertions.assertTrue(receiver.isListening());
        }
    }

    @Test
    void testCloses() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        PeerConnectionReceiver receiver = null;
        try {
            receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000, messengerFactory);
            receiver.close();
        } finally {
            Assertions.assertFalse(receiver.isListening());
        }
    }

    @Test
    void testCloseAgain() throws IOException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        PeerConnectionReceiver receiver = null;
        try {
            receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000, messengerFactory);
            receiver.close();
            Assertions.assertDoesNotThrow(receiver::close);
        } finally {

        }
    }

    @Test
    void testConnectClient() throws IOException, InterruptedException {
        final MessengerFactory messengerFactory = TestUtilities.createMessengerFactory();
        final List<PeerConnection> connections = new ArrayList<>();
        try (final PeerConnectionReceiver receiver = PeerConnectionReceiver.createBound("127.0.0.1", 9000,
                messengerFactory); final Socket userSocket = new Socket()) {
            receiver.setPeerConnectedListener(connections::add);
            Assertions.assertDoesNotThrow(() -> userSocket.connect(new InetSocketAddress("127.0.0.1", 9000)));
            // I can't be sure that the receiver processed the connection immediatly, I give
            // it some time to finish accepting the user...
            Thread.sleep(Duration.ofMillis(250));
            Assertions.assertEquals(1, connections.size());
        }
    }
}
