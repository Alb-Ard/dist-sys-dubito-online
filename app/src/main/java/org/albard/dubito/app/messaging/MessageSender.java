package org.albard.dubito.app.messaging;

import java.io.OutputStream;
import java.util.function.Function;

public interface MessageSender {
    static MessageSender createFromStream(final OutputStream stream, final Function<Object, byte[]> serializer) {
        return SerialMessageSender.createFromStream(stream, serializer);
    }

    void sendMessage(Object message);
}
