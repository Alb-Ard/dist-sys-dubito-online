package org.albard.dubito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessageSerializer;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.PingMessage;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerEndPointPair;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;

public final class TestUtilities {
    private TestUtilities() {
    }

    public static ServerSocket createAndLaunchSocketServer(final String bindAddress, final int bindPort)
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

    public static PeerNetwork createAndLaunchServerNetwork(final String bindAddress, final int bindPort)
            throws UnknownHostException, IOException {
        return PeerNetwork.createBound(PeerId.createNew(), bindAddress, bindPort, createMessengerFactory());
    }

    public static PeerEndPoint createEndPoint(final int port) {
        return PeerEndPoint.ofValues("127.0.0.1", port);
    }

    public static PeerEndPointPair createEndPointPair(final int localPort, final int remotePort) {
        return new PeerEndPointPair(createEndPoint(localPort), createEndPoint(remotePort));
    }

    public static GameMessage createMessage() {
        return new PingMessage(PeerId.createNew(), Set.of(PeerId.createNew()));
    }

    public static MessengerFactory createMessengerFactory() {
        return new MessengerFactory(MessageSerializer.createJson());
    }

    public static MessageSerializer createMessageSerializer(final GameMessage deserializedMessage,
            final byte[] serializedData) {
        return new MessageSerializer() {
            @Override
            public Optional<GameMessage> deserialize(final byte[] message) {
                return Optional.ofNullable(deserializedMessage);
            }

            @Override
            public byte[] serialize(GameMessage message) {
                return serializedData;
            }
        };
    }

    public static <X extends GameMessage> List<X> addMessageListener(final Class<X> messageClass,
            final MessageReceiver receiver) {
        final List<X> received = new ArrayList<>();
        receiver.addMessageListener(m -> {
            if (messageClass.isAssignableFrom(m.getClass())) {
                received.add(messageClass.cast(m));
                return true;
            }
            return false;
        });
        return received;
    }
}
