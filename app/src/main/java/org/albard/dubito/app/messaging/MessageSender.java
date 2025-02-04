package org.albard.dubito.app.messaging;

import java.util.function.Consumer;

public interface MessageSender {
    static MessageSender create(Consumer<Object> sendHandler) {
        return SerialMessageSender.create(sendHandler);
    }

    void send(Object message);
}
