package org.albard.dubito;

import static org.albard.dubito.TestUtilities.withCloseable;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.albard.dubito.messaging.MessageSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class MessageSenderTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> MessageSender.createFromStream(new ByteArrayOutputStream(0), m -> new byte[0]));
    }

    @Test
    void testSendSingle() throws Exception {
        final var expectedMessage = TestUtilities.createMessage();
        final var serializer = TestUtilities.createMessageSerializer();
        withCloseable(() -> new ByteArrayOutputStream(10), stream -> {
            final MessageSender sender = MessageSender.createFromStream(stream, serializer::serialize);
            sender.sendMessage(expectedMessage);
            Assertions.assertArrayEquals(serializer.serialize(expectedMessage), stream.toByteArray());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 5, 10 })
    void testSendMultipleSynchronous(final int messageCount) throws Exception {
        final var expectedMessage = TestUtilities.createMessage();
        final var serializer = TestUtilities.createMessageSerializer();
        final var expectedData = serializer.serialize(expectedMessage);
        withCloseable(() -> new ByteArrayOutputStream(expectedData.length * messageCount), stream -> {
            final MessageSender sender = MessageSender.createFromStream(stream, serializer::serialize);
            Stream.iterate(0, i -> i + 1).limit(messageCount).forEach(x -> sender.sendMessage(expectedMessage));
            final byte[] writtenData = stream.toByteArray();
            Assertions.assertEquals(expectedData.length * messageCount, writtenData.length);
            for (int i = 0; i < messageCount; i++) {
                final byte[] writtenItem = new byte[expectedData.length];
                System.arraycopy(writtenData, i * expectedData.length, writtenItem, 0, expectedData.length);
                Assertions.assertArrayEquals(expectedData, writtenItem);
            }
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 5, 10 })
    void testSendMultipleAsynchronous(final int messageCount) throws Exception {
        final var expectedMessage = TestUtilities.createMessage();
        final var serializer = TestUtilities.createMessageSerializer();
        final var expectedData = serializer.serialize(expectedMessage);
        withCloseable(() -> new ByteArrayOutputStream(expectedData.length * messageCount), stream -> {
            final MessageSender sender = MessageSender.createFromStream(stream, serializer::serialize);
            final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            for (int i = 0; i < messageCount; i++) {
                executor.submit(() -> sender.sendMessage(expectedMessage)).get();
            }
            final byte[] writtenData = stream.toByteArray();
            Assertions.assertEquals(expectedData.length * messageCount, writtenData.length);
            final byte[] writtenItem = new byte[expectedData.length];
            for (int i = 0; i < messageCount; i++) {
                System.arraycopy(writtenData, i * expectedData.length, writtenItem, 0, expectedData.length);
                Assertions.assertArrayEquals(expectedData, writtenItem);
            }
        });
    }
}
