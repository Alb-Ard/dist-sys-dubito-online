package org.albard.dubito.app;

import java.io.ByteArrayOutputStream;

import org.albard.dubito.app.messaging.MessageSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserMessageSenderTest {

    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> MessageSender.createFromStream(new ByteArrayOutputStream(0), m -> new byte[0]));
    }

    @Test
    void testSend() {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(10);
        final MessageSender sender = MessageSender.createFromStream(stream, m -> m.toString().getBytes());
        sender.send("Test");
        Assertions.assertArrayEquals("Test".getBytes(), stream.toByteArray());
    }
}
