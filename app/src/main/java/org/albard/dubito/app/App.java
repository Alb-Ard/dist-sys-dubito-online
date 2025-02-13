package org.albard.dubito.app;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;

import org.albard.dubito.app.messaging.MessageSerializer;
import org.albard.dubito.app.messaging.MessengerFactory;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.handlers.MessageHandlerChain;
import org.albard.dubito.app.messaging.messages.PingMessage;
import org.albard.dubito.app.network.PeerEndPoint;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerNetwork;
import org.albard.dubito.app.network.PeerStarNetwork;

import org.albard.dubito.app.messaging.MessageSender;

public class App {
    public static void main(final String[] args) {
        final PeerId localPeerId = PeerId.createNew();
        final PeerEndPoint serverEndPoint = readServerEndPointFromArgs(args);
        final PeerEndPoint clientEndPoint = readClientEndPointFromArgs(args);
        final MessageSerializer messageSerializer = MessageSerializer.createJson();
        final MessengerFactory messengerFactory = new MessengerFactory(messageSerializer);
        try (final PeerNetwork network = PeerStarNetwork.createBound(localPeerId, serverEndPoint.getHost(),
                serverEndPoint.getPort(), messengerFactory)) {

            network.addMessageListener(createIncomingMessageHandler(network, localPeerId));
            System.out.println("[SERVER] Listening on " + serverEndPoint);
            Thread.sleep(Duration.ofSeconds(1));
            while (true) {
                try {
                    runClient(clientEndPoint, network, localPeerId, messageSerializer);
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

    private static PeerEndPoint readClientEndPointFromArgs(final String[] args) {
        final Scanner inputScanner = new Scanner(System.in);
        final String remoteAddress = getArgOrDefault(args, 2,
                () -> requestInput(inputScanner, "[CLIENT] Insert remote peer hostname: "));
        final int remotePort = parseIntOrDefault(
                getArgOrDefault(args, 3,
                        () -> requestInput(inputScanner, "[CLIENT] Insert remote peer port (or empty for default): ")),
                () -> 9000);
        return PeerEndPoint.createFromValues(remoteAddress, remotePort);
    }

    private static PeerEndPoint readServerEndPointFromArgs(final String[] args) {
        final String bindAddress = getArgOrDefault(args, 0, () -> "0.0.0.0");
        final int bindPort = Integer.parseInt(getArgOrDefault(args, 1, () -> "9000"));
        return PeerEndPoint.createFromValues(bindAddress, bindPort);
    }

    private static void runClient(final PeerEndPoint endPoint, final PeerNetwork network, final PeerId localPeerId,
            final MessageSerializer messageSerializer) {
        try {
            System.out.println("[CLIENT] Opening connection " + endPoint);
            if (!network.connectToPeer(endPoint)) {
                System.out.println("[CLIENT] Connection failed!");
                return;
            }
            System.out.println("[CLIENT] Initialized!");
            do {
                System.out.println("[CLIENT] Press any key to send a ping to all");
                System.in.read();
                System.out.println("[CLIENT] Sending Ping");
                network.sendMessage(new PingMessage(localPeerId, Set.of()));
            } while (true);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("[CLIENT] Closed");
    }

    private static MessageHandler createIncomingMessageHandler(final MessageSender messageSender,
            final PeerId localPeerId) {
        final MessageHandler fallbackHandler = m -> {
            System.out.println("[" + m.getSender() + "] " + m);
            return true;
        };
        return new MessageHandlerChain(List.of(fallbackHandler));
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
