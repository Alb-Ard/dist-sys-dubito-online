package org.albard.dubito.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.albard.dubito.app.messaging.UserMessageReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserMessageReceiverTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> UserMessageReceiver.createFromStream(new ByteArrayInputStream(new byte[0]), String::new).close());
    }

    @Test
    void testStart() throws IOException {
        try (final UserMessageReceiver receiver = UserMessageReceiver
                .createFromStream(new ByteArrayInputStream(new byte[0]), String::new)) {
            Assertions.assertDoesNotThrow(() -> receiver.start());
        }
    }

    @Test
    void testStartAgain() throws IOException {
        try (final UserMessageReceiver receiver = UserMessageReceiver
                .createFromStream(new ByteArrayInputStream(new byte[0]), String::new)) {
            receiver.start();
            Assertions.assertThrows(Exception.class, () -> receiver.start());
        }
    }

    @Test
    void testRestart() throws IOException {
        try (final UserMessageReceiver receiver = UserMessageReceiver
                .createFromStream(new ByteArrayInputStream(new byte[0]), String::new)) {
            receiver.start();
            receiver.close();
            Assertions.assertThrows(Exception.class, () -> receiver.start());
        }
    }

    @Test
    void testCloseAgain() throws IOException {
        final UserMessageReceiver receiver = UserMessageReceiver.createFromStream(new ByteArrayInputStream(new byte[0]),
                String::new);
        receiver.start();
        receiver.close();
        Assertions.assertDoesNotThrow(() -> receiver.close());
    }

    @Test
    void testReceive() throws IOException, InterruptedException {
        try (final UserMessageReceiver receiver = UserMessageReceiver
                .createFromStream(new ByteArrayInputStream("Test".getBytes()), String::new)) {
            receiver.setMessageListener((m) -> Assertions.assertEquals("Test", m));
            receiver.start();
            // Let the receiver handle the message
            Thread.sleep(Duration.ofMillis(500));
        }
    }
}
