package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.albard.dubito.app.messaging.MessageSender;
import org.albard.dubito.app.messaging.MessageSenderFactory;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserMessageSenderTest {

    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> MessageSender.create(m -> {
        }));
    }

    @Test
    void testSend() {
        final List<Object> receivedMessages = new LinkedList<>();
        final MessageSender sender = MessageSender.create(receivedMessages::add);
        sender.send("Test");
        Assertions.assertEquals(1, receivedMessages.size());
    }

    @Test
    void testFactoryCreateSocket() throws IOException {
        final MessageSenderFactory factory = new MessageSenderFactory();
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("0.0.0.0", 9000)) {
            Assertions.assertDoesNotThrow(
                    () -> factory.createSocketSender(new Socket("127.0.0.1", 9000), new MessageSerializer<Socket>() {
                        public byte[] serialize(InetSocketAddress user, Socket connection, Object message) {
                            return message.toString().getBytes();
                        };

                        @Override
                        public Object deserialize(InetSocketAddress user, Socket connection, byte[] message) {
                            return new String(message);
                        }
                    }));
        }
    }
}
