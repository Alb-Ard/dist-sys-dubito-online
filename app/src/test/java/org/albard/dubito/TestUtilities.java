package org.albard.dubito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.PingMessage;
import org.albard.dubito.messaging.serialization.MessageSerializer;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerEndPointPair;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.utils.Listeners;

public final class TestUtilities {
    @FunctionalInterface
    public interface SupplierByIndex<X> {
        X get(int index) throws Exception;
    }

    @FunctionalInterface
    public interface ConsumerAction<X> {
        void accept(X item) throws Exception;
    }

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
        return new PingMessage(PeerId.createNew(), Set.of(PeerId.createNew()),
                PeerEndPoint.ofValues("127.0.0.1", 9000));
    }

    public static MessengerFactory createMessengerFactory() {
        return new MessengerFactory(MessageSerializer.createJson());
    }

    public static ObjectSerializer<GameMessage> createMessageSerializer(final GameMessage deserializedMessage,
            final byte[] serializedData) {
        final ObjectSerializer<GameMessage> serializer = mock();
        when(serializer.deserialize(any(), any())).thenReturn(Optional.of(deserializedMessage));
        when(serializer.serialize(any())).thenReturn(serializedData);
        return serializer;
    }

    public static <X extends GameMessage> List<X> addMessageListener(final Class<X> messageClass,
            final MessageReceiver receiver) {
        return addMessageListener(messageClass, receiver, null);
    }

    public static <X extends GameMessage> List<X> addMessageListener(final Class<X> messageClass,
            final MessageReceiver receiver, final Consumer<X> receivedListener) {
        final List<X> received = new ArrayList<>();
        receiver.addMessageListener(m -> {
            if (messageClass.isAssignableFrom(m.getClass())) {
                final var message = messageClass.cast(m);
                Listeners.run(receivedListener, message);
                received.add(message);
                return true;
            }
            return false;
        });
        return received;
    }

    public static <X extends AutoCloseable> void withMultiCloseable(final int count,
            final SupplierByIndex<X> itemSupplier, final ConsumerAction<List<X>> action) throws Exception {
        final List<X> items = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                items.add(itemSupplier.get(i));
            }
            action.accept(items);
        } finally {
            items.forEach(x -> {
                try {
                    x.close();
                } catch (final Exception ex) {
                }
            });
        }
    }
}
