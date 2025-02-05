package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

import org.albard.dubito.app.messaging.MessageDispatcher;
import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageDispatcherTest {
    private static class MockMessenger implements MessageSender, MessageReceiver {
        @Override
        public void setMessageListener(Consumer<Object> listener) {
        }

        @Override
        public void start() {
        }

        @Override
        public void send(Object message) {
        }
    }

    @Test
    void testCreate() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        Assertions.assertEquals(0, dispatcher.getMessengerCount());
    }

    @Test
    void testAddMessenger() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        Assertions.assertDoesNotThrow(() -> dispatcher.addMessenger(new InetSocketAddress(0), messenger, messenger));
        Assertions.assertDoesNotThrow(() -> dispatcher.addMessenger(new InetSocketAddress(1), messenger, messenger));
        Assertions.assertEquals(2, dispatcher.getMessengerCount());
    }

    @Test
    void testAddMessengerAgain() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        dispatcher.addMessenger(new InetSocketAddress(0), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.addMessenger(new InetSocketAddress(0), messenger, messenger));
        Assertions.assertEquals(1, dispatcher.getMessengerCount());
    }

    @Test
    void testRemoveMessenger() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        dispatcher.addMessenger(new InetSocketAddress(0), messenger, messenger);
        dispatcher.addMessenger(new InetSocketAddress(1), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.removeMessenger(new InetSocketAddress(0)));
        Assertions.assertEquals(1, dispatcher.getMessengerCount());
    }

    @Test
    void testRemoveNonExistingMessenger() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        dispatcher.addMessenger(new InetSocketAddress(0), messenger, messenger);
        dispatcher.addMessenger(new InetSocketAddress(1), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.removeMessenger(new InetSocketAddress(2)));
        Assertions.assertEquals(2, dispatcher.getMessengerCount());
    }

    @Test
    void testRemoveMessengerWhenEmpty() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        Assertions.assertDoesNotThrow(() -> dispatcher.removeMessenger(new InetSocketAddress(9)));
        Assertions.assertEquals(0, dispatcher.getMessengerCount());
    }
}
