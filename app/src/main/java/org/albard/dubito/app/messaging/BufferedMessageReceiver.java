package org.albard.dubito.app.messaging;

import java.io.InputStream;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.albard.dubito.app.ObservableCloseable;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class BufferedMessageReceiver implements MessageReceiver, ObservableCloseable {
    private final Thread receiveThread;
    private final Lock messageListenersLock = new ReentrantLock();
    private final Set<MessageHandler> messageListeners = new HashSet<>();
    private final Set<ClosedListener> closedListeners = Collections.synchronizedSet(new HashSet<>());
    private final Queue<GameMessage> bufferedMessages = new LinkedList<>();

    public BufferedMessageReceiver(final InputStream stream, final Function<byte[], GameMessage> deserializer) {
        this.receiveThread = Thread.ofVirtual().unstarted(() -> {
            final byte[] buffer = new byte[1024];
            while (true) {
                try {
                    final int readByteCount = stream.read(buffer);
                    if (readByteCount <= 0) {
                        break;
                    }
                    final byte[] messageBuffer = new byte[readByteCount];
                    System.arraycopy(buffer, 0, messageBuffer, 0, readByteCount);
                    final GameMessage message = deserializer.apply(messageBuffer);
                    try {
                        messageListenersLock.lock();
                        if (this.messageListeners.isEmpty()) {
                            this.bufferedMessages.add(message);
                        } else {
                            this.messageListeners.forEach(l -> l.handleMessage(message));
                        }
                    } finally {
                        this.messageListenersLock.unlock();
                    }
                } catch (final SocketException ex) {
                    System.err.println(ex.getMessage());
                    break;
                } catch (final Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
            this.closedListeners.forEach(l -> l.closed());
        });
    }

    static MessageReceiver createFromStream(final InputStream stream,
            final Function<byte[], GameMessage> deserializer) {
        final BufferedMessageReceiver receiver = new BufferedMessageReceiver(stream, deserializer);
        receiver.start();
        return receiver;
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.messageListenersLock.lock();
        final int previousListenerCount = this.messageListeners.size();
        this.messageListeners.add(listener);
        if (previousListenerCount <= 0) {
            while (!this.bufferedMessages.isEmpty()) {
                this.messageListeners.forEach(l -> l.handleMessage(this.bufferedMessages.remove()));
            }
        }
        this.messageListenersLock.unlock();
    }

    @Override
    public void removeMessageListener(final MessageHandler listener) {
        this.messageListenersLock.lock();
        this.messageListeners.remove(listener);
        this.messageListenersLock.unlock();
    }

    @Override
    public void addClosedListener(final ClosedListener listener) {
        this.closedListeners.add(listener);
    }

    @Override
    public void removeClosedListener(final ClosedListener listener) {
        this.closedListeners.remove(listener);
    }

    private void start() {
        this.receiveThread.start();
    }
}
