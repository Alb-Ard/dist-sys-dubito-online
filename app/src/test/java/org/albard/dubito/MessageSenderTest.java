package org.albard.dubito;

import java.io.ByteArrayOutputStream;

import org.albard.dubito.messaging.MessageSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageSenderTest {

    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> MessageSender.createFromStream(new ByteArrayOutputStream(0), m -> new byte[0]));
    }

    @Test
    void testSend() {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(10);
        final MessageSender sender = MessageSender.createFromStream(stream, TestUtilities
                .createMockMessageSerializer(TestUtilities.createMockMessage(), "Test".getBytes())::serialize);
        sender.sendMessage(TestUtilities.createMockMessage());
        Assertions.assertArrayEquals("Test".getBytes(), stream.toByteArray());
    }
}
