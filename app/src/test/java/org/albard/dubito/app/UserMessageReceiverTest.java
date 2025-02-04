package org.albard.dubito.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.albard.dubito.app.messaging.MessageReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserMessageReceiverTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> MessageReceiver.createFromStream(new ByteArrayInputStream(new byte[0]), String::new));
    }

    @Test
    void testStart() throws IOException {
        try (final InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream, String::new);
            Assertions.assertDoesNotThrow(() -> receiver.start());
        }
    }

    @Test
    void testStartAgain() throws IOException {
        try (final InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream, String::new);
            receiver.start();
            Assertions.assertThrows(Exception.class, () -> receiver.start());
        }
    }

    @Test
    void testReceive() throws IOException, InterruptedException {
        try (final InputStream inputStream = new ByteArrayInputStream("Test".getBytes())) {
            final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream, String::new);
            receiver.setMessageListener((m) -> Assertions.assertEquals("Test", m));
            receiver.start();
            // Let the receiver handle the message
            Thread.sleep(Duration.ofMillis(500));
        }
    }
}
