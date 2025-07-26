package org.albard.dubito;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.albard.dubito.lobby.server.LobbyServer;
import org.albard.dubito.messaging.MessageReceiver;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.PingMessage;
import org.albard.dubito.messaging.serialization.MessageSerializer;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.dubito.userManagement.server.UserServer;
import org.albard.dubito.userManagement.server.UserService;
import org.albard.utils.Listeners;

public final class TestUtilities {
    @FunctionalInterface
    public interface UnsafeSupplierByIndex<X> {
        X get(int index) throws Exception;
    }

    @FunctionalInterface
    public interface UnsafeSupplier<X> {
        X get() throws Exception;
    }

    @FunctionalInterface
    public interface UnsafeConsumer<X> {
        void accept(X item) throws Exception;
    }

    @FunctionalInterface
    public interface UnsafeRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface UnsafeBiConsumer<X, Y> {
        void accept(X a, Y b) throws Exception;
    }

    private TestUtilities() {
    }

    public static PeerEndPoint createEndPoint(final int port) {
        return PeerEndPoint.ofValues("127.0.0.1", port);
    }

    public static GameMessage createMessage() {
        return new PingMessage(PeerId.createNew(), Set.of(PeerId.createNew()),
                PeerEndPoint.ofValues("127.0.0.1", 9000));
    }

    public static MessengerFactory createMessengerFactory() {
        return new MessengerFactory(MessageSerializer.createJson());
    }

    public static ObjectSerializer<GameMessage> createMessageSerializer() {
        final ObjectSerializer<GameMessage> serializer = MessageSerializer.createJson();
        return serializer;
    }

    public static <X extends GameMessage> List<X> addMessageListener(final Class<X> baseMessageClass,
            final MessageReceiver receiver) {
        return addMessageListener(baseMessageClass, receiver, null);
    }

    public static <X extends GameMessage> List<X> addMessageListener(final Class<X> baseMessageClass,
            final MessageReceiver receiver, final Consumer<X> receivedListener) {
        final List<X> received = Collections.synchronizedList(new ArrayList<>());
        receiver.addMessageListener(m -> {
            if (baseMessageClass.isAssignableFrom(m.getClass())) {
                final X message = baseMessageClass.cast(m);
                Optional.ofNullable(receivedListener).ifPresent(l -> Listeners.run(l, message));
                received.add(message);
            }
            return false;
        });
        return received;
    }

    public static <X extends AutoCloseable> void withCloseable(final UnsafeSupplier<X> itemSupplier,
            final UnsafeConsumer<X> action) throws Exception {
        withCloseable(itemSupplier, action, null, null);
    }

    public static <X extends AutoCloseable> void withCloseable(final UnsafeSupplier<X> itemSupplier,
            final UnsafeConsumer<X> action, final UnsafeConsumer<X> beforeCloseAction,
            final UnsafeRunnable afterCloseAction) throws Exception {
        X item = null;
        try {
            item = itemSupplier.get();
            action.accept(item);
        } finally {
            if (item != null) {
                if (beforeCloseAction != null) {
                    try {
                        beforeCloseAction.accept(item);
                    } catch (final Exception ex) {
                    }
                }
                try {
                    item.close();
                    Thread.sleep(50);
                } catch (final Exception ex) {
                }
                if (afterCloseAction != null) {
                    try {
                        afterCloseAction.run();
                    } catch (final Exception ex) {
                    }
                }
            }
        }
    }

    public static <X extends AutoCloseable> void withMultiCloseable(final int count,
            final UnsafeSupplierByIndex<X> itemSupplier, final UnsafeConsumer<List<X>> action) throws Exception {
        final List<X> items = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                items.add(itemSupplier.get(i));
            }
            action.accept(List.copyOf(items));
        } finally {
            items.forEach(x -> {
                try {
                    x.close();
                    Thread.sleep(50);
                } catch (final Exception ex) {
                }
            });
        }
    }

    public static void withSocketServer(final String bindAddress, final int bindPort,
            final UnsafeConsumer<ServerSocket> action) throws Exception {
        final Thread[] listerThread = new Thread[] { null };
        final List<Socket> clients = new ArrayList<>();
        withCloseable(() -> {
            final ServerSocket socket = new ServerSocket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(bindAddress, bindPort));
            return socket;
        }, server -> {
            listerThread[0] = Thread.ofVirtual().unstarted(() -> {
                try {
                    while (!server.isClosed()) {
                        clients.add(server.accept());
                    }
                } catch (final Exception ex) {
                }
            });
            listerThread[0].start();
            action.accept(server);
        }, x -> {
            for (final Socket client : clients) {
                try {
                    client.close();
                } catch (final Exception ex) {
                }
            }
        }, () -> {
            if (listerThread[0] != null) {
                listerThread[0].join();
            }
        });
    }

    public static void withSocketClient(final String bindAddress, final int bindPort, final String remoteAddress,
            final int remotePort, final UnsafeConsumer<Socket> action) throws Exception {
        withCloseable(() -> {
            final Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(bindAddress, bindPort));
            socket.connect(new InetSocketAddress(remoteAddress, remotePort));
            return socket;
        }, action);
    }

    public static void withNetwork(final PeerId peerId, final String bindAddress, final int bindPort,
            final UnsafeConsumer<PeerNetwork> action) throws Exception {
        withNetwork(peerId, bindAddress, bindPort, createMessengerFactory(), action);
    }

    public static void withNetwork(final PeerId peerId, final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory, final UnsafeConsumer<PeerNetwork> action) throws Exception {
        withCloseable(() -> PeerNetwork.createBound(peerId, bindAddress, bindPort, messengerFactory), action);
    }

    public static void withUserServer(final PeerNetwork network, final UnsafeBiConsumer<UserService, UserServer> action)
            throws Exception {
        final UserService userService = new UserService();
        action.accept(userService, new UserServer(network, userService));
    }

    public static void withLobbyServer(final PeerNetwork network, final UnsafeConsumer<LobbyServer> action)
            throws Exception {
        withUserServer(network, (userService, x) -> action.accept(new LobbyServer(network, userService)));
    }
}
