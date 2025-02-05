package org.albard.dubito.app.messaging;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

import org.albard.dubito.app.messaging.messages.GameMessage;

public interface MessageReceiver {
    static MessageReceiver createFromStream(final InputStream stream,
            final Function<byte[], GameMessage> deserializer) {
        return BufferedMessageReceiver.createFromStream(stream, deserializer);
    }

    void setMessageListener(Consumer<GameMessage> listener);

    void start();
}
