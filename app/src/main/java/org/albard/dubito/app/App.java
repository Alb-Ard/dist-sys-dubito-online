package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.albard.dubito.app.connection.UserConnection;
import org.albard.dubito.app.connection.UserConnectionReceiver;
import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.PingMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.albard.dubito.app.messaging.MessageDispatcher;
import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;

public class App {
    public static void main(final String[] args) {
        final String bindAddress = getArgOrDefault(args, 0, () -> "0.0.0.0");
        final int bindPort = Integer.parseInt(getArgOrDefault(args, 1, () -> "9000"));
        final MessageSerializer messageSerializer = createMessageSerializer();
        final ObservableMap<UserEndPoint, UserConnection> connections = ObservableMap.createEmpty();
        final MessageDispatcher messageDispatcher = new MessageDispatcher();
        messageDispatcher.start();
        connections.addListener(createUserRepositoryListener(messageSerializer, messageDispatcher));
        try (final UserConnectionReceiver connectionReceiver = UserConnectionReceiver.createBound(bindAddress,
                bindPort)) {
            connectionReceiver.setUserConnectedListener(c -> connections
                    .putIfAbsent(UserEndPoint.createFromAddress(c.getSocket().getRemoteSocketAddress()), c));
            connectionReceiver.start();
            System.out.println("[SERVER] Listening on " + bindAddress + ":" + bindPort);
            Thread.sleep(Duration.ofSeconds(1));
            runClient(args, 2, connections, messageDispatcher, messageDispatcher);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("[SERVER] Closed");
    }

    private static void runClient(final String[] args, final int argsOffset,
            final ObservableMap<UserEndPoint, UserConnection> connectionRepository,
            final MessageReceiver messageReceiver, final MessageSender messageSender) {
        final Scanner inputScanner = new Scanner(System.in);
        final String remoteAddress = getArgOrDefault(args, argsOffset,
                () -> requestInput(inputScanner, "[CLIENT] Insert remote peer hostname: "));
        final int remotePort = parseIntOrDefault(
                getArgOrDefault(args, argsOffset + 1,
                        () -> requestInput(inputScanner, "[CLIENT] Insert remote peer port (or empty for default): ")),
                () -> 9000);
        final UserEndPoint remoteEndPoint = UserEndPoint.createFromValues(remoteAddress, remotePort);
        System.out.println("[CLIENT] Connecting to " + remoteEndPoint);
        try (final UserConnection connection = UserConnection.createAndConnect(remoteAddress, remotePort)) {
            connectionRepository.putIfAbsent(remoteEndPoint, connection);
            messageReceiver.setMessageListener(createIncomingMessageHandler(remoteEndPoint));
            System.out.println("[CLIENT] Connected!");
            do {
                Thread.sleep(Duration.ofSeconds(5));
                System.out.println("[CLIENT] Sending Ping");
                messageSender.sendMessage(new PingMessage(remoteEndPoint, Set.of(UserEndPoint.BROADCAST)));
            } while (true);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("[CLIENT] Closed");
    }

    private static ObservableMapListener<UserEndPoint, UserConnection> createUserRepositoryListener(
            final MessageSerializer messageSerializer, final MessageDispatcher messageDispatcher) {
        return new ObservableMapListener<>() {
            @Override
            public void entryAdded(UserEndPoint endPoint, UserConnection connection) {
                try {
                    final MessageReceiver receiver = MessageReceiver
                            .createFromStream(connection.getSocket().getInputStream(), messageSerializer::deserialize);
                    final MessageSender sender = MessageSender
                            .createFromStream(connection.getSocket().getOutputStream(), messageSerializer::serialize);
                    messageDispatcher.addMessenger(endPoint, sender, receiver);
                    System.out.println("[SERVER] Opened connection from " + endPoint);
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void entryRemoved(UserEndPoint endPoint, UserConnection connection) {
                try {
                    System.out.println("[SERVER] Closed connection from " + endPoint);
                    messageDispatcher.removeMessenger(endPoint);
                    connection.close();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private static Consumer<GameMessage> createIncomingMessageHandler(UserEndPoint endPoint) {
        return m -> {
            System.out.println("[" + endPoint + "] " + m);
        };
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
