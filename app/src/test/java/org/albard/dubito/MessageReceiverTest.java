package org.albard.dubito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.messages.GameMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageReceiverTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> MessageReceiver.createFromStream(new ByteArrayInputStream(new byte[0]), TestUtilities
                        .createMockMessageSerializer(TestUtilities.createMockMessage(), new byte[0])::deserialize));
    }

    @Test
    void testReceive() throws IOException, InterruptedException {
        final GameMessage expectedMessage = TestUtilities.createMockMessage();
        try (final InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream,
                    TestUtilities.createMockMessageSerializer(expectedMessage, "Test".getBytes())::deserialize);
            receiver.addMessageListener(m -> {
                Assertions.assertEquals(expectedMessage, m);
                return true;
            });
            // Let the receiver handle the message
            Thread.sleep(Duration.ofMillis(500));
        }
    }
}
