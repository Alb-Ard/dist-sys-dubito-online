package org.albard.dubito.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.connection.PeerConnectionReceiver;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.handlers.MessageHandlerChain;
import org.albard.dubito.app.messaging.handlers.PingPongMessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.PingMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.albard.dubito.app.messaging.MessageDispatcher;
import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;

public class App {
    public static void main(final String[] args) {
        final PeerId localPeerId = PeerId.createNew();
        final PeerEndPoint serverEndPoint = readServerEndPointFromArgs(args);
        final PeerEndPointPair clientEndPoint = readClientEndPointFromArgs(args);
        final MessageSerializer messageSerializer = createMessageSerializer();
        final MessageDispatcher messageDispatcher = new MessageDispatcher();
        messageDispatcher.setMessageListener(createIncomingMessageHandler(messageDispatcher, localPeerId));
        messageDispatcher.start();
        try (final PeerConnectionReceiver connectionReceiver = PeerConnectionReceiver
                .createBound(serverEndPoint.getHost(), serverEndPoint.getPort())) {
            final Consumer<PeerConnection> connectionListener = createConnectionsListener(localPeerId,
                    messageSerializer, messageDispatcher);
            connectionReceiver.setPeerConnectedListener(connectionListener);
            connectionReceiver.start();
            System.out.println("[SERVER] Listening on " + serverEndPoint);
            Thread.sleep(Duration.ofSeconds(1));
            while (true) {
                try {
                    runClient(clientEndPoint, localPeerId, messageDispatcher, messageSerializer);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
                Thread.sleep(Duration.ofSeconds(1));
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("[SERVER] Closed");
    }

    private static PeerEndPointPair readClientEndPointFromArgs(final String[] args) {
        final String bindAddress = "127.0.0.1";
        final int bindPort = 0;
        final Scanner inputScanner = new Scanner(System.in);
        final String remoteAddress = getArgOrDefault(args, 2,
                () -> requestInput(inputScanner, "[CLIENT] Insert remote peer hostname: "));
        final int remotePort = parseIntOrDefault(
                getArgOrDefault(args, 3,
                        () -> requestInput(inputScanner, "[CLIENT] Insert remote peer port (or empty for default): ")),
                () -> 9000);
        return new PeerEndPointPair(PeerEndPoint.createFromValues(bindAddress, bindPort),
                PeerEndPoint.createFromValues(remoteAddress, remotePort));
    }

    private static PeerEndPoint readServerEndPointFromArgs(final String[] args) {
        final String bindAddress = getArgOrDefault(args, 0, () -> "0.0.0.0");
        final int bindPort = Integer.parseInt(getArgOrDefault(args, 1, () -> "9000"));
        return PeerEndPoint.createFromValues(bindAddress, bindPort);
    }

    private static void runClient(final PeerEndPointPair endPoint, final PeerId localPeerId,
            final MessageDispatcher messageDispatcher, final MessageSerializer messageSerializer) {
        System.out.println("[CLIENT] Opening connection " + endPoint);
        try (final PeerConnection connection = PeerConnection.createAndConnect(endPoint.getLocalEndPoint().getHost(),
                endPoint.getLocalEndPoint().getPort(), endPoint.getRemoteEndPoint().getHost(),
                endPoint.getRemoteEndPoint().getPort())) {
            System.out.println("[CLIENT] Connected! Sending my id...");
            sendPeerIdToConnection(localPeerId, connection);
            final PeerId remotePeerId = waitForConnectionPeerId(connection);
            addPeerToDispatcher(messageDispatcher, remotePeerId, connection, messageSerializer);
            System.out.println("[CLIENT] Initialized!");
            do {
                Thread.sleep(Duration.ofSeconds(5));
                System.out.println("[CLIENT] Sending Ping");
                messageDispatcher.sendMessage(new PingMessage(localPeerId, Set.of()));
            } while (true);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("[CLIENT] Closed");
    }

    private static void sendPeerIdToConnection(final PeerId localPeerId, final PeerConnection connection)
            throws IOException {
        // Non blocking, since we may want to do a send-receive of peer ids
        Thread.ofVirtual().start(() -> {
            try {
                final Socket socket = connection.getSocket();
                System.out.println("[SERVER] Sending peer id " + localPeerId + " " + socket.getRemoteSocketAddress());
                final OutputStream stream = socket.getOutputStream();
                stream.write(localPeerId.getBytes());
                stream.flush();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static PeerId waitForConnectionPeerId(final PeerConnection connection) throws IOException {
        final Socket socket = connection.getSocket();
        final InputStream stream = socket.getInputStream();
        final PeerId id = PeerId.createFromBytes(stream.readNBytes(PeerId.LENGTH));
        // Clean the input buffer
        while (stream.available() > 0) {
            stream.read();
        }
        System.out.println("[SERVER] Received peer id " + id + " from " + socket.getRemoteSocketAddress());
        return id;
    }

    private static Consumer<PeerConnection> createConnectionsListener(final PeerId localPeerId,
            final MessageSerializer messageSerializer, final MessageDispatcher messageDispatcher) {
        return new Consumer<>() {
            @Override
            public void accept(final PeerConnection connection) {
                try {
                    System.out.println("[SERVER] Connecting to " + connection.getSocket().getRemoteSocketAddress());
                    sendPeerIdToConnection(localPeerId, connection);
                    final PeerId remotePeerId = waitForConnectionPeerId(connection);
                    addPeerToDispatcher(messageDispatcher, remotePeerId, connection, messageSerializer);
                    System.out.println("[SERVER] Connected to " + remotePeerId);
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private static void addPeerToDispatcher(final MessageDispatcher dispatcher, final PeerId peerId,
            final PeerConnection connection, final MessageSerializer messageSerializer) {
        try {
            final MessageReceiver receiver = MessageReceiver.createFromStream(connection.getSocket().getInputStream(),
                    messageSerializer::deserialize);
            final MessageSender sender = MessageSender.createFromStream(connection.getSocket().getOutputStream(),
                    messageSerializer::serialize);
            dispatcher.addPeer(peerId, sender, receiver);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private static MessageHandler createIncomingMessageHandler(final MessageSender messageSender,
            final PeerId localPeerId) {
        final MessageHandler fallbackHandler = m -> {
            System.out.println("[" + m.getSender() + "] " + m);
            return true;
        };
        return new MessageHandlerChain(
                List.of(new PingPongMessageHandler(localPeerId, messageSender), fallbackHandler));
    }

    private static MessageSerializer createMessageSerializer() {
        return new MessageSerializer() {
            private final ObjectMapper jsonMapper = new ObjectMapper();

            @Override
            public byte[] serialize(final GameMessage message) {
                try {
                    return this.jsonMapper.writeValueAsBytes(this.jsonMapper.createObjectNode()
                            .put("className", message.getClass().getName()).putPOJO("messageBody", message));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    return new byte[0];
                }
            }

            @Override
            public GameMessage deserialize(final byte[] rawMessage) {
                try {
                    final JsonNode jsonRoot = this.jsonMapper.readTree(rawMessage);
                    final String className = jsonRoot.get("className").asText();
                    final Class<?> messageClass = GameMessage.class.getClassLoader().loadClass(className);
                    return (GameMessage) this.jsonMapper.treeToValue(jsonRoot.get("messageBody"), messageClass);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        };
    }

    private static int parseIntOrDefault(final String value, Supplier<Integer> defaultValueProvider) {
        try {
            return Integer.parseInt(value);
        } catch (final Exception ex) {
            return defaultValueProvider.get();
        }
    }

    private static String requestInput(final Scanner scanner, final String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    private static String getArgOrDefault(String[] args, int index, Supplier<String> defaultValueProvider) {
        if (args.length <= index) {
            return defaultValueProvider.get();
        }
        return args[index];
    }
}
