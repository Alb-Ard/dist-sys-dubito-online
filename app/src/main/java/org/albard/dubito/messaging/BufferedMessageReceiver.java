package org.albard.dubito.messaging;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.utils.Locked;
import org.albard.utils.Logger;
import org.albard.utils.ObservableCloseable;

public final class BufferedMessageReceiver implements MessageReceiver, ObservableCloseable {
    private final record ReceiverState(Set<MessageHandler> handlers, Set<MessageHandler> handlersToRemove,
            Queue<GameMessage> bufferedMessages) {
    }

    private final Thread receiveThread;
    private final Locked<ReceiverState> state = Locked
            .of(new ReceiverState(new HashSet<>(), new HashSet<>(), new LinkedList<>()));
    private final Set<ClosedListener> closedListeners = Collections.synchronizedSet(new HashSet<>());

    public BufferedMessageReceiver(final InputStream stream,
            final Function<InputStream, Optional<GameMessage>> deserializer) {
        this.receiveThread = Thread.ofVirtual().unstarted(() -> {
            while (true) {
                try {
                    final boolean isSuccess = deserializer.apply(stream).map(message -> {
                        this.state.exchange(s -> {
                            if (s.handlers.isEmpty()) {
                                s.bufferedMessages.add(message);
                                return s;
                            }
                            MessageReceiver.handleMessageAndUpdateHandlers(message, s.handlers, s.handlersToRemove);
                            return s;
                        });
                        return true;
                    }).orElse(false);
                    if (!isSuccess) {
                        Logger.logInfo("Message deserialize failed, closing...");
                        break;
                    }
                } catch (final Exception ex) {
                    Logger.logError("Message deserialize failed (" + ex.getClass().getSimpleName() + ":"
                            + ex.getMessage() + "), closing...");
                    break;
                }
            }
            this.closedListeners.forEach(l -> l.closed());
        });
    }

    static MessageReceiver createFromStream(final InputStream stream,
            final Function<InputStream, Optional<GameMessage>> deserializer) {
        final BufferedMessageReceiver receiver = new BufferedMessageReceiver(stream, deserializer);
        receiver.start();
        return receiver;
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.state.exchange(s -> {
            final int previousCount = s.handlers.size();
            s.handlersToRemove.remove(listener);
            s.handlers.add(listener);
            if (previousCount <= 0) {
                while (!s.bufferedMessages.isEmpty()) {
                    final GameMessage message = s.bufferedMessages.remove();
                    MessageReceiver.handleMessageAndUpdateHandlers(message, s.handlers, s.handlersToRemove);
                }
            }
            return s;
        });
    }

    @Override
    public void queueRemoveMessageListener(final MessageHandler listener) {
        this.state.exchange(s -> {
            s.handlersToRemove.add(listener);
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
