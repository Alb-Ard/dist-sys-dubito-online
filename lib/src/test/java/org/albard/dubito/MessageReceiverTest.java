package org.albard.dubito;

import static org.albard.dubito.TestUtilities.addMessageListener;
import static org.albard.dubito.TestUtilities.withCloseable;

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.serialization.ObjectSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class MessageReceiverTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> MessageReceiver.createFromStream(new ByteArrayInputStream(new byte[0]),
                x -> TestUtilities.createMessageSerializer().deserialize(x, GameMessage.class)));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5 })
    void testReceiveMultipleBeforeListening(int messageCount) throws Exception {
        final GameMessage expectedMessage = TestUtilities.createMessage();
        final ObjectSerializer<GameMessage> serializer = TestUtilities.createMessageSerializer();
        final byte[] expectedData = serializer.serialize(expectedMessage);
        withCloseable(() -> new PipedOutputStream(), outputStream -> {
            withCloseable(() -> new PipedInputStream(outputStream), inputStream -> {
                final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream,
                        x -> serializer.deserialize(x, GameMessage.class));
                for (int i = 0; i < messageCount; i++) {
                    outputStream.write(expectedData);
                }
                outputStream.flush();
                final List<GameMessage> receivedMessages = addMessageListener(GameMessage.class, receiver);
                Thread.sleep(200);
                Assertions.assertEquals(0, inputStream.available());
                Assertions.assertEquals(messageCount, receivedMessages.size());
                for (int i = 0; i < messageCount; i++) {
                    Assertions.assertEquals(expectedMessage, receivedMessages.get(i));
                }
            });
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5 })
    void testReceiveMultipleWithIndividualFlush(int messageCount) throws Exception {
        final GameMessage expectedMessage = TestUtilities.createMessage();
        final ObjectSerializer<GameMessage> serializer = TestUtilities.createMessageSerializer();
        final byte[] expectedData = serializer.serialize(expectedMessage);
        withCloseable(() -> new PipedOutputStream(), outputStream -> {
            withCloseable(() -> new PipedInputStream(outputStream), inputStream -> {
                final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream,
                        x -> serializer.deserialize(x, GameMessage.class));
                final List<GameMessage> receivedMessages = addMessageListener(GameMessage.class, receiver);
                for (int i = 0; i < messageCount; i++) {
                    outputStream.write(expectedData);
                    outputStream.flush();
                }
                Thread.sleep(50);
                Assertions.assertEquals(0, inputStream.available());
                Assertions.assertEquals(messageCount, receivedMessages.size());
                for (int i = 0; i < messageCount; i++) {
                    Assertions.assertEquals(expectedMessage, receivedMessages.get(i));
                }
            });
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 5 })
    void testReceiveMultipleWithIndividualFlushSpaced(int messageCount) throws Exception {
        final GameMessage expectedMessage = TestUtilities.createMessage();
        final ObjectSerializer<GameMessage> serializer = TestUtilities.createMessageSerializer();
        final byte[] expectedData = serializer.serialize(expectedMessage);
        withCloseable(() -> new PipedOutputStream(), outputStream -> {
            withCloseable(() -> new PipedInputStream(outputStream), inputStream -> {
                final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream,
                        x -> serializer.deserialize(x, GameMessage.class));
                final List<GameMessage> receivedMessages = addMessageListener(GameMessage.class, receiver);
                for (int i = 0; i < messageCount; i++) {
                    outputStream.write(expectedData);
                    outputStream.flush();
                    Thread.sleep(100);
                }
                Assertions.assertEquals(0, inputStream.available());
                Assertions.assertEquals(messageCount, receivedMessages.size());
                for (int i = 0; i < messageCount; i++) {
                    Assertions.assertEquals(expectedMessage, receivedMessages.get(i));
                }
            });
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 5 })
    void testReceiveMultipleWithSingleFlush(int messageCount) throws Exception {
        final GameMessage expectedMessage = TestUtilities.createMessage();
        final ObjectSerializer<GameMessage> serializer = TestUtilities.createMessageSerializer();
        final byte[] expectedData = serializer.serialize(expectedMessage);
        withCloseable(() -> new PipedOutputStream(), outputStream -> {
            withCloseable(() -> new PipedInputStream(outputStream), inputStream -> {
                final MessageReceiver receiver = MessageReceiver.createFromStream(inputStream,
                        x -> serializer.deserialize(x, GameMessage.class));
                final List<GameMessage> receivedMessages = addMessageListener(GameMessage.class, receiver);
                for (int i = 0; i < messageCount; i++) {
                    outputStream.write(expectedData);
                }
                outputStream.flush();
                Thread.sleep(50);
                Assertions.assertEquals(0, inputStream.available());
                Assertions.assertEquals(messageCount, receivedMessages.size());
                for (int i = 0; i < messageCount; i++) {
                    Assertions.assertEquals(expectedMessage, receivedMessages.get(i));
                }
            });
        });
    }
}
