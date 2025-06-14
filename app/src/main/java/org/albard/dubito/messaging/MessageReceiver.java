package org.albard.dubito.messaging;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

import org.albard.dubito.messaging.handlers.MessageHandler;
import org.albard.dubito.messaging.messages.GameMessage;

public interface MessageReceiver {
    static MessageReceiver createFromStream(final InputStream stream,
            final Function<byte[], List<GameMessage>> deserializer) {
        return BufferedMessageReceiver.createFromStream(stream, deserializer);
    }

    void addMessageListener(MessageHandler listener);

    void removeMessageListener(MessageHandler listener);

    default void addOnceMessageListener(final MessageHandler handler) {
        this.addMessageListener(new MessageHandler() {
            @Override
            public boolean handleMessage(final GameMessage message) {
                if (!handler.handleMessage(message)) {
                    return false;
                }
                MessageReceiver.this.removeMessageListener(this);
                return true;
            }
        });
    }
}
