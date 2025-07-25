package org.albard.dubito;

import static org.albard.dubito.TestUtilities.withCloseable;
import static org.albard.dubito.TestUtilities.withSocketClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.connection.PeerConnectionReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PeerConnectionReceiverTest {

    @Test
    void testStartReceiving() throws Exception {
        withCloseable(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, TestUtilities.createMessengerFactory()),
                receiver -> Assertions.assertTrue(receiver.isListening()));
    }

    @Test
    void testCloses() throws Exception {
        final PeerConnectionReceiver[] receiver = new PeerConnectionReceiver[] { null };
        withCloseable(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, TestUtilities.createMessengerFactory()),
                x -> receiver[0] = x);
        Assertions.assertFalse(receiver[0].isListening());
    }

    @Test
    void testCloseAgain() throws Exception {
        final PeerConnectionReceiver[] receiver = new PeerConnectionReceiver[] { null };
        withCloseable(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, TestUtilities.createMessengerFactory()),
                x -> receiver[0] = x);
        Assertions.assertDoesNotThrow(receiver[0]::close);
    }

    @Test
    void testConnectClient() throws Exception {
        final List<PeerConnection> connections = new ArrayList<>();
        withCloseable(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, TestUtilities.createMessengerFactory()),
                receiver -> {
                    receiver.setPeerConnectedListener(connections::add);
                    receiver.setPeerDisconnectedListener(connections::remove);
                    withSocketClient("127.0.0.1", 9001, "127.0.0.1", 9000, userSocket -> {
                        // I can't be sure that the receiver processed the connection immediately, I
                        // give it some time to finish accepting the user...
                        Thread.sleep(Duration.ofMillis(150));
                        Assertions.assertEquals(1, connections.size());
                    });
                });
    }

    @Test
    void testDisconnectClient() throws Exception {
        final List<PeerConnection> connections = new ArrayList<>();
        withCloseable(
                () -> PeerConnectionReceiver.createBound("127.0.0.1", 9000, TestUtilities.createMessengerFactory()),
                receiver -> {
                    receiver.setPeerConnectedListener(connections::add);
                    receiver.setPeerDisconnectedListener(connections::remove);
                    withSocketClient("127.0.0.1", 9001, "127.0.0.1", 9000, userSocket -> {
                        // I can't be sure that the receiver processed the connection immediately, I
                        // give it some time to finish accepting the user...
                        Thread.sleep(Duration.ofMillis(150));
                    });
                    Thread.sleep(Duration.ofMillis(150));
                    Assertions.assertEquals(0, connections.size());
                });
    }
}
