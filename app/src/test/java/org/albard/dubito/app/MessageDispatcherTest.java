package org.albard.dubito.app;

import org.albard.dubito.app.messaging.MessageDispatcher;
import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageDispatcherTest {
    private static class MockMessenger implements MessageSender, MessageReceiver {
        @Override
        public void setMessageListener(MessageHandler listener) {
        }

        @Override
        public void start() {
        }

        @Override
        public void sendMessage(GameMessage message) {
        }
    }

    @Test
    void testCreate() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        Assertions.assertEquals(0, dispatcher.getPeerCount());
    }

    @Test
    void testAddMessenger() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        Assertions.assertDoesNotThrow(() -> dispatcher.addPeer(PeerId.createNew(), messenger, messenger));
        Assertions.assertDoesNotThrow(() -> dispatcher.addPeer(PeerId.createNew(), messenger, messenger));
        Assertions.assertEquals(2, dispatcher.getPeerCount());
    }

    @Test
    void testAddMessengerAgain() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        final PeerId peerId = PeerId.createNew();
        dispatcher.addPeer(peerId, messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.addPeer(peerId, messenger, messenger));
        Assertions.assertEquals(1, dispatcher.getPeerCount());
    }

    @Test
    void testRemoveMessenger() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        final PeerId peerId = PeerId.createNew();
        dispatcher.addPeer(peerId, messenger, messenger);
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.removePeer(peerId));
        Assertions.assertEquals(1, dispatcher.getPeerCount());
    }

    @Test
    void testRemoveNonExistingMessenger() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        final MockMessenger messenger = new MockMessenger();
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.removePeer(PeerId.createNew()));
        Assertions.assertEquals(2, dispatcher.getPeerCount());
    }

    @Test
    void testRemoveMessengerWhenEmpty() {
        final MessageDispatcher dispatcher = new MessageDispatcher();
        Assertions.assertDoesNotThrow(() -> dispatcher.removePeer(PeerId.createNew()));
        Assertions.assertEquals(0, dispatcher.getPeerCount());
    }
}
