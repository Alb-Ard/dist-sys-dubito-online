package org.albard.dubito.app;

import java.net.InetSocketAddress;

public final class SerialMessageSender<T> implements MessageSender<T> {
    private final UserConnectionRepository<T> repository;
    private final MessageHandler<T> sendHandler;

    public SerialMessageSender(final UserConnectionRepository<T> repository,
            final MessageSender.MessageHandler<T> sendHandler) {
        this.repository = repository;
        this.sendHandler = sendHandler;
    }

    static <T> MessageSender<T> create(final UserConnectionRepository<T> repository,
            final MessageSender.MessageHandler<T> sendHandler) {
        return new SerialMessageSender<T>(repository, sendHandler);
    }

    @Override
    public void sendToAll(final Object message) {
        this.sendTo(message, this.repository.getAllUserEndPoints());
    }

    @Override
    public void sendTo(final Object message, final InetSocketAddress[] userEndPoints) {
        for (final InetSocketAddress userEndPoint : userEndPoints) {
            final T connection = this.repository.getUser(userEndPoint);
            this.sendHandler.send(userEndPoint, connection, message);
        }
    }
}
