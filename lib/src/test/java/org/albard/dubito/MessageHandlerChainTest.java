package org.albard.dubito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.handlers.MessageHandlerChain;
import org.albard.dubito.messaging.messages.GameMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageHandlerChainTest {
    private static class MockHandler implements MessageHandler {
        @Override
        public boolean handleMessage(final GameMessage message) {
            return false;
        }
    }

    @Test
    void testCreateEmpty() {
        Assertions.assertDoesNotThrow(() -> new MessageHandlerChain(Collections.emptyList()));
    }

    @Test
    void testCreateFilled() {
        Assertions.assertDoesNotThrow(() -> new MessageHandlerChain(List.of(new MockHandler(), new MockHandler())));
    }

    @Test
    void testCreateNull() {
        Assertions.assertThrows(Exception.class, () -> new MessageHandlerChain(null));
    }

    @Test
    void testExecuteChainSuccess() {
        final List<Object> messages = new ArrayList<>();
        final GameMessage message = TestUtilities.createMessage();
        final MessageHandlerChain chain = new MessageHandlerChain(
                List.of(new MockHandler(), m -> messages.add(m), new MockHandler(), m -> messages.add(m)));
        Assertions.assertTrue(chain.handleMessage(message));
        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals(message, messages.get(0));
    }

    @Test
    void testExecuteChainFailure() {
        final MessageHandlerChain chain = new MessageHandlerChain(
                List.of(new MockHandler(), new MockHandler(), new MockHandler()));
        Assertions.assertFalse(chain.handleMessage(TestUtilities.createMessage()));
    }
}
