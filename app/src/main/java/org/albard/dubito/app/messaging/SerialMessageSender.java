package org.albard.dubito.app.messaging;

import java.util.function.Consumer;

public final class SerialMessageSender implements MessageSender {
    private final Consumer<Object> sendHandler;

    public SerialMessageSender(final Consumer<Object> sendHandler) {
        this.sendHandler = sendHandler;
    }

    static MessageSender create(final Consumer<Object> sendHandler) {
        return new SerialMessageSender(sendHandler);
    }

    @Override
    public void send(final Object message) {
        this.sendHandler.accept(message);
    }
}
