package org.albard.dubito.app.messaging;

import java.io.InputStream;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.albard.dubito.app.Locked;
import org.albard.dubito.app.ObservableCloseable;
import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class BufferedMessageReceiver implements MessageReceiver, ObservableCloseable {
    private final record MessageListenersState(Set<MessageHandler> listeners, Queue<GameMessage> bufferedMessages) {
    }

    private final Thread receiveThread;
    private final Locked<MessageListenersState> messageListenersState = Locked
            .of(new MessageListenersState(new HashSet<>(), new LinkedList<>()));
    private final Set<ClosedListener> closedListeners = Collections.synchronizedSet(new HashSet<>());

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
                    this.messageListenersState.exchange(s -> {
                        if (s.listeners.isEmpty()) {
                            s.bufferedMessages.add(message);
                        } else {
                            s.listeners.forEach(l -> l.handleMessage(message));
                        }
                        return s;
                    });
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
        this.messageListenersState.exchange(s -> {
            final int previousListenerCount = s.listeners.size();
            s.listeners.add(listener);
            if (previousListenerCount <= 0) {
                while (!s.bufferedMessages.isEmpty()) {
                    final GameMessage message = s.bufferedMessages.remove();
                    s.listeners.forEach(l -> l.handleMessage(message));
                }
            }
            return s;
        });
    }

    @Override
    public void removeMessageListener(final MessageHandler listener) {
        this.messageListenersState.exchange(s -> {
            s.listeners.remove(listener);
            return s;
        });
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
