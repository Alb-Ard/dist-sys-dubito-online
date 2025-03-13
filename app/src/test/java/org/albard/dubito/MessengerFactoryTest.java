package org.albard.dubito;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessageSender;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.messages.PingMessage;
import org.albard.dubito.network.PeerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessengerFactoryTest {
    @Test
    void testCreateSender() throws IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchSocketServer("127.0.0.1", 9000);
                final Socket socket = new Socket("127.0.0.1", 9000)) {
            final MessengerFactory factory = new MessengerFactory(
                    TestUtilities.createMessageSerializer(new PingMessage(PeerId.createNew(), Set.of()), new byte[0]));
            final MessageSender sender = factory.createSender(socket);
            Assertions.assertNotNull(sender);
        }
    }

    @Test
    void testCreateReceiver() throws IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchSocketServer("127.0.0.1", 9000);
                final Socket socket = new Socket("127.0.0.1", 9000)) {
            final MessengerFactory factory = new MessengerFactory(
                    TestUtilities.createMessageSerializer(new PingMessage(PeerId.createNew(), Set.of()), new byte[0]));
            final MessageReceiver receiver = factory.createReceiver(socket);
            Assertions.assertNotNull(receiver);
        }
    }
}
