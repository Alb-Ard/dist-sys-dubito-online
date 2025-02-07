package org.albard.dubito.app;

import java.util.Set;

import org.albard.dubito.app.messaging.HashMapMessageDispatcher;
import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.PingMessage;
import org.albard.dubito.app.network.PeerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageDispatcherTest {
    private static class MockMessenger implements MessageSender, MessageReceiver {
        private MessageHandler listener;

        @Override
        public void addMessageListener(MessageHandler listener) {
            this.listener = listener;
        }

        @Override
        public void removeMessageListener(MessageHandler listener) {
            this.listener = null;
        }

        @Override
        public void start() {
        }

        @Override
        public void sendMessage(GameMessage message) {
            if (this.listener != null) {
                this.listener.handleMessage(message);
            }
        }
    }

    @Test
    void testCreate() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        Assertions.assertEquals(0, dispatcher.getPeerCount());
    }

    @Test
    void testAddMessenger() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        final MockMessenger messenger = new MockMessenger();
        Assertions.assertDoesNotThrow(() -> dispatcher.addPeer(PeerId.createNew(), messenger, messenger));
        Assertions.assertDoesNotThrow(() -> dispatcher.addPeer(PeerId.createNew(), messenger, messenger));
        Assertions.assertEquals(2, dispatcher.getPeerCount());
    }

    @Test
    void testAddMessengerAgain() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        final MockMessenger messenger = new MockMessenger();
        final PeerId peerId = PeerId.createNew();
        dispatcher.addPeer(peerId, messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.addPeer(peerId, messenger, messenger));
        Assertions.assertEquals(1, dispatcher.getPeerCount());
    }

    @Test
    void testRemoveMessenger() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        final MockMessenger messenger = new MockMessenger();
        final PeerId peerId = PeerId.createNew();
        dispatcher.addPeer(peerId, messenger, messenger);
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.removePeer(peerId));
        Assertions.assertEquals(1, dispatcher.getPeerCount());
    }

    @Test
    void testRemoveNonExistingMessenger() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        final MockMessenger messenger = new MockMessenger();
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        Assertions.assertDoesNotThrow(() -> dispatcher.removePeer(PeerId.createNew()));
        Assertions.assertEquals(2, dispatcher.getPeerCount());
    }

    @Test
    void testRemoveMessengerWhenEmpty() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        Assertions.assertDoesNotThrow(() -> dispatcher.removePeer(PeerId.createNew()));
        Assertions.assertEquals(0, dispatcher.getPeerCount());
    }

    @Test
    void testLoopProtection() {
        final HashMapMessageDispatcher dispatcher = new HashMapMessageDispatcher(PeerId.createNew());
        // ID of the peer that sends this message
        final PeerId senderPeerId = PeerId.createNew();
        // Messenger of ANOTHER peer that will receive the message
        final MockMessenger messenger = new MockMessenger();
        messenger.addMessageListener(m -> {
            // If the message reaches here, it means that I've received the message I've
            // sent!
            Assertions.assertNotEquals(m.getSender(), senderPeerId);
            return true;
        });
        // Add the messenger with a different ID
        dispatcher.addPeer(PeerId.createNew(), messenger, messenger);
        dispatcher.sendMessage(new PingMessage(senderPeerId, Set.of()));
    }
}
