package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.handlers.MessageHandlerChain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageHandlerChainTest {
    private static class MockHandler implements MessageHandler {
        @Override
        public boolean handleMessage(InetSocketAddress fromEndPoint, Object message) {
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
        final Object message = new Object();
        final MessageHandlerChain chain = new MessageHandlerChain(
                List.of(new MockHandler(), (u, m) -> messages.add(m), new MockHandler(), (u, m) -> messages.add(m)));
        Assertions.assertTrue(chain.handleMessage(null, message));
        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals(message, messages.get(0));
    }

    @Test
    void testExecuteChainFailure() {
        final MessageHandlerChain chain = new MessageHandlerChain(
                List.of(new MockHandler(), new MockHandler(), new MockHandler()));
        Assertions.assertFalse(chain.handleMessage(null, new Object()));
    }
}
