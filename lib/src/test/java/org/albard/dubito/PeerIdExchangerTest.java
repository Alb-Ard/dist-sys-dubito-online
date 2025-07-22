package org.albard.dubito;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.albard.dubito.messaging.Messenger;
import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.PingMessage;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerExchanger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PeerIdExchangerTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> new PeerExchanger(PeerId.createNew(), PeerEndPoint.ofValues("127.0.0.1", 9000)));
    }

    @Test
    void testSimpleExchange() throws IOException {
        final Messenger messenger = new Messenger() {
            MessageHandler handler;

            @Override
            public void addMessageListener(MessageHandler listener) {
                this.handler = listener;
            }

            @Override
            public void queueRemoveMessageListener(MessageHandler listener) {
            }

            @Override
            public void sendMessage(GameMessage message) {
                this.handler.handleMessage(
                        new PingMessage(PeerId.createNew(), null, PeerEndPoint.ofValues("127.0.0.1", 9000)));
            }

        };
        final PeerExchanger exchanger = new PeerExchanger(PeerId.createNew(), PeerEndPoint.ofValues("127.0.0.1", 9000));
        Assertions.assertTrue(exchanger.exchangeIds(messenger).isPresent());
    }

    @Test
    void testConcurrentExchange() throws IOException, InterruptedException {
        final int exchangerCount = 10;
        final PeerExchanger exchanger = new PeerExchanger(PeerId.createNew(), PeerEndPoint.ofValues("127.0.0.1", 9000));
        final List<Optional<Map.Entry<PeerId, PeerEndPoint>>> exchangedData = new ArrayList<>();
        for (int i = 0; i < exchangerCount; i++) {
            final int index = i;
            final Messenger messenger = new Messenger() {
                private MessageHandler handler;

                @Override
                public void addMessageListener(MessageHandler listener) {
                    this.handler = listener;
                }

                @Override
                public void queueRemoveMessageListener(MessageHandler listener) {
                }

                @Override
                public void sendMessage(GameMessage message) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            Thread.sleep(Duration.ofMillis(new Random().nextLong(100, 1000)));
                        } catch (final InterruptedException ex) {
                        }
                        this.handler.handleMessage(new PingMessage(new PeerId(Integer.toString(index)), null,
                                PeerEndPoint.ofValues("127.0.0.1", index)));
                    });
                }
            };
            Thread.ofVirtual().start(() -> exchangedData.add(exchanger.exchangeIds(messenger)));
        }

        Thread.sleep(Duration.ofSeconds(2));

        Assertions.assertEquals(exchangerCount, exchangedData.size());
        for (int i = 0; i < exchangerCount; i++) {
            final var data = exchangedData.get(i);
            Assertions.assertTrue(data.isPresent());
            Assertions.assertEquals(new PeerId(Integer.toString(i)), data.get().getKey());
            Assertions.assertEquals(PeerEndPoint.ofValues("127.0.0.1", i), data.get().getValue());
        }
    }
}
