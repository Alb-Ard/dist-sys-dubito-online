package org.albard.dubito.app.network;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.albard.dubito.app.messaging.Messenger;
import org.albard.dubito.app.messaging.messages.PingMessage;

public final class PeerIdExchanger {
    private static final Duration EXCHANGE_TIMEOUT = Duration.ofSeconds(3);

    private final PeerId localPeerId;

    public PeerIdExchanger(final PeerId localPeerId) {
        this.localPeerId = localPeerId;
    }

    public PeerId getLocalPeerId() {
        return this.localPeerId;
    }

    public Optional<PeerId> exchangeIds(final Messenger messenger) {
        try {
            final Semaphore lock = new Semaphore(0);
            final PeerId[] receivedId = new PeerId[] { null };
            messenger.addOnceMessageListener(m -> {
                System.out.println(
                        localPeerId + ": Received message " + m.getClass().getSimpleName() + " from " + m.getSender());
                receivedId[0] = m.getSender();
                lock.release();
                return true;
            });
            this.sendPingToConnection(messenger);
            if (lock.tryAcquire(EXCHANGE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                System.out.println(localPeerId + ": Connection " + messenger + " has id " + receivedId[0]);
            } else {
                System.err.println(localPeerId + ": Connection " + messenger + " did not respond to my ping");
            }
            return Optional.ofNullable(receivedId[0]);
        } catch (final Exception ex) {
            System.err.println(localPeerId + ": Could not exchange Ids: " + ex.getMessage());
        }
        return Optional.empty();
    }

    private void sendPingToConnection(final Messenger messenger) throws IOException {
        System.out.println(localPeerId + ": Sending out ping to " + messenger);
        messenger.sendMessage(new PingMessage(localPeerId, Set.of()));
    }
}
