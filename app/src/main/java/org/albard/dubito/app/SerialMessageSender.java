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
        final InetSocketAddress[] userEndPoints = this.repository.getAllUserEndPoints();
        for (final InetSocketAddress userEndPoint : userEndPoints) {
            this.sendHandler.send(this.repository.getUser(userEndPoint), message);
        }
    }
}
