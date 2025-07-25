package org.albard.dubito;

import static org.albard.dubito.TestUtilities.withSocketClient;
import static org.albard.dubito.TestUtilities.withSocketServer;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessageSender;
import org.albard.dubito.messaging.MessengerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessengerFactoryTest {
    @Test
    void testCreateSender() throws Exception {
        withSocketServer("127.0.0.1", 9000, server -> {
            withSocketClient("127.0.0.1", 9001, "127.0.0.1", 9000, socket -> {
                final MessengerFactory factory = new MessengerFactory(TestUtilities.createMessageSerializer());
                final MessageSender sender = factory.createSender(socket);
                Assertions.assertNotNull(sender);
            });
        });
    }

    @Test
    void testCreateReceiver() throws Exception {
        withSocketServer("127.0.0.1", 9000, server -> {
            withSocketClient("127.0.0.1", 9001, "127.0.0.1", 9000, socket -> {
                final MessengerFactory factory = new MessengerFactory(TestUtilities.createMessageSerializer());
                final MessageReceiver receiver = factory.createReceiver(socket);
                Assertions.assertNotNull(receiver);
            });
        });
    }
}
