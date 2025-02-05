package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Set;

import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.PingMessage;

public final class TestUtilities {
    private TestUtilities() {
    }

    public static ServerSocket createAndLaunchServer(final String bindAddress, final int bindPort)
            throws UnknownHostException, IOException {
        final ServerSocket server = new ServerSocket(bindPort, 4, InetAddress.getByName(bindAddress));
        Thread.ofVirtual().start(() -> {
            try {
                while (!server.isClosed()) {
                    server.accept();
                }
            } catch (final Exception e) {
            }
        });
        return server;
    }

    public static GameMessage createMockMessage() {
        return new PingMessage(UserEndPoint.createFromValues("127.0.0.1", 1),
                Set.of(UserEndPoint.createFromValues("127.0.0.1", 2)));
    }

    public static MessageSerializer createMockMessageSerializer(final GameMessage deserializedMessage,
            final byte[] serializedData) {
        return new MessageSerializer() {
            @Override
            public GameMessage deserialize(final byte[] message) {
                return deserializedMessage;
            }

            @Override
            public byte[] serialize(GameMessage message) {
                return serializedData;
            }
        };
    }
}
