package org.albard.dubito.app;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.albard.dubito.app.messaging.Messenger;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.PingMessage;
import org.albard.dubito.app.network.PeerId;
import org.albard.dubito.app.network.PeerIdExchanger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PeerIdExchangerTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> new PeerIdExchanger(PeerId.createNew()));
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
            public void removeMessageListener(MessageHandler listener) {
            }

            @Override
            public void sendMessage(GameMessage message) {
                this.handler.handleMessage(new PingMessage(PeerId.createNew(), null));
            }

        };
        final PeerIdExchanger exchanger = new PeerIdExchanger(PeerId.createNew());
        Assertions.assertNotNull(exchanger.exchangeIds(messenger));
    }

    @Test
    void testConcurrentExchange() throws IOException, InterruptedException {
        final int exchangerCount = 10;
        final PeerIdExchanger exchanger = new PeerIdExchanger(PeerId.createNew());
        final List<PeerId> exchangedIds = new ArrayList<>();
        for (int i = 0; i < exchangerCount; i++) {
            final Messenger messenger = new Messenger() {
                MessageHandler handler;

                @Override
                public void addMessageListener(MessageHandler listener) {
                    this.handler = listener;
                }

                @Override
                public void removeMessageListener(MessageHandler listener) {
                }

                @Override
                public void sendMessage(GameMessage message) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            Thread.sleep(Duration.ofMillis(new Random().nextLong(100, 1000)));
                        } catch (final InterruptedException ex) {
                        }
                        this.handler.handleMessage(new PingMessage(PeerId.createNew(), null));
                    });
                }
            };
            Thread.ofVirtual().start(() -> exchangedIds.add(exchanger.exchangeIds(messenger)));
        }

        Thread.sleep(Duration.ofSeconds(2));

        Assertions.assertEquals(exchangerCount, exchangedIds.size());
        exchangedIds.forEach(Assertions::assertNotNull);
    }
}
