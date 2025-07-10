package org.albard.dubito.messaging;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.utils.Logger;

public interface MessageReceiver {
    static MessageReceiver createFromStream(final InputStream stream,
            final Function<InputStream, Optional<GameMessage>> deserializer) {
        return BufferedMessageReceiver.createFromStream(stream, deserializer);
    }

    void addMessageListener(MessageHandler listener);

    void queueRemoveMessageListener(MessageHandler listener);

    default void addOnceMessageListener(final MessageHandler handler) {
        this.addMessageListener(new MessageHandler() {
            @Override
            public boolean handleMessage(final GameMessage message) {
                if (!handler.handleMessage(message)) {
                    return false;
                }
                MessageReceiver.this.queueRemoveMessageListener(this);
                return true;
            }
        });
    }

    static boolean handleMessageAndUpdateHandlers(final GameMessage message, final Set<MessageHandler> handlersToInvoke,
            final Set<MessageHandler> handlersToRemove) {
        handlersToInvoke.removeAll(handlersToRemove);
        handlersToRemove.clear();
        final boolean wasHandled = handlersToInvoke.stream().map(l -> {
            try {
                return l.handleMessage(message);
            } catch (final Exception ex) {
                Logger.logError(
                        "Error invoking message handler: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                return false;
            }
        }).anyMatch(x -> x);
        return wasHandled;
    }
}
