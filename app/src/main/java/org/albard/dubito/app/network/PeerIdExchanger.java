package org.albard.dubito.app.network;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.albard.dubito.app.connection.PeerConnection;
import org.albard.dubito.app.messaging.messages.PingMessage;

public final class PeerIdExchanger {
    private static final Duration EXCHANGE_TIMEOUT = Duration.ofSeconds(10);

    private final PeerId localPeerId;
    private final ExecutorService executor;

    public PeerIdExchanger(final PeerId localPeerId) {
        this.localPeerId = localPeerId;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public PeerId exchangeIds(final PeerConnection connection) {
        try {
            final Semaphore lock = new Semaphore(0);
            final PeerId[] receivedId = new PeerId[] { null };
            connection.addOnceMessageListener(m -> {
                System.out.println(localPeerId + ": Received message " + m.getClass().getSimpleName() + " from "
                        + connection.getSocket().getRemoteSocketAddress());
                if (m instanceof PingMessage ping) {
                    receivedId[0] = ping.getSender();
                    lock.release();
                    return true;
                }
                return false;
            });
            this.sendPingToConnection(connection);
            lock.tryAcquire(EXCHANGE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            return receivedId[0];
        } catch (final Exception ex) {
            System.err.println("Could not exchange Ids: " + ex.getMessage());
        }
        return null;
    }

    private void sendPingToConnection(final PeerConnection connection) throws IOException {
        // Non blocking, since we may want to do a send-receive of peer ids
        System.out.println(localPeerId + ": Sending out ping to " + connection.getSocket().getRemoteSocketAddress());
        this.executor.submit(() -> connection.sendMessage(new PingMessage(localPeerId, Set.of())));
    }
}
