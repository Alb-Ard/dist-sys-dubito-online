package org.albard.dubito.messaging;

import java.io.OutputStream;
import java.util.function.Function;

import org.albard.dubito.messaging.messages.GameMessage;

public interface MessageSender {
    static MessageSender createFromStream(final OutputStream stream, final Function<GameMessage, byte[]> serializer) {
        return SerialMessageSender.createFromStream(stream, serializer);
    }

    void sendMessage(GameMessage message);
}
