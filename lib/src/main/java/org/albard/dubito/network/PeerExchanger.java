package org.albard.dubito.network;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.albard.dubito.messaging.Messenger;
import org.albard.dubito.messaging.messages.PingMessage;
import org.albard.utils.Logger;

public final class PeerExchanger {
    private static final Duration EXCHANGE_TIMEOUT = Duration.ofSeconds(3);

    private final PeerId localPeerId;
    private final PeerEndPoint localListenEndPoint;

    public PeerExchanger(final PeerId localPeerId, final PeerEndPoint localListenEndPoint) {
        this.localPeerId = localPeerId;
        this.localListenEndPoint = localListenEndPoint;
    }

    public PeerId getLocalPeerId() {
        return this.localPeerId;
    }

    public Optional<Map.Entry<PeerId, PeerEndPoint>> exchangeIds(final Messenger messenger) {
        try {
            final Semaphore lock = new Semaphore(0);
            final PingMessage[] receivedPing = new PingMessage[] { null };
            messenger.addOnceMessageListener(m -> {
                if (m instanceof PingMessage pingMessage) {
                    Logger.logInfo(localPeerId + ": Received message " + m.getClass().getSimpleName() + " from "
                            + m.getSender());
                    receivedPing[0] = pingMessage;
                    lock.release();
                    return true;
                }
                return false;
            });
            this.sendPingToConnection(messenger);
            if (!lock.tryAcquire(EXCHANGE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                Logger.logError(localPeerId + ": Connection " + messenger + " did not respond to my ping");
                return Optional.empty();
            }
            Logger.logInfo(localPeerId + ": Connection " + messenger + " has id " + receivedPing[0].getSender()
                    + " and endpoint " + receivedPing[0].getSenderServerEndPoint());
            return Optional
                    .ofNullable(Map.entry(receivedPing[0].getSender(), receivedPing[0].getSenderServerEndPoint()));
        } catch (final Exception ex) {
            Logger.logError(localPeerId + ": Could not exchange Ids: " + ex.getMessage());
        }
        return Optional.empty();
    }

    private void sendPingToConnection(final Messenger messenger) throws IOException {
        Logger.logInfo(localPeerId + ": Sending out ping to " + messenger);
        messenger.sendMessage(new PingMessage(this.localPeerId, Set.of(), this.localListenEndPoint));
    }
}
