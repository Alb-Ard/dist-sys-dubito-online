package org.albard.dubito.app.messaging;

import java.net.InetSocketAddress;

import org.albard.dubito.app.UserConnectionRepository;

public final class SerialMessageSender<T> implements UserMessageSender<T> {
    private final UserConnectionRepository<T> repository;
    private final MessageHandler<T> sendHandler;

    public SerialMessageSender(final UserConnectionRepository<T> repository,
            final UserMessageSender.MessageHandler<T> sendHandler) {
        this.repository = repository;
        this.sendHandler = sendHandler;
    }

    static <T> UserMessageSender<T> create(final UserConnectionRepository<T> repository,
            final UserMessageSender.MessageHandler<T> sendHandler) {
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
