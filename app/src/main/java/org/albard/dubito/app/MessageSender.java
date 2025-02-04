package org.albard.dubito.app;

import java.net.InetSocketAddress;

public interface MessageSender<T> {
    @FunctionalInterface
    public interface MessageHandler<T> {
        void send(InetSocketAddress endPoint, T connection, Object message);
    }

    static <T> MessageSender<T> create(UserConnectionRepository<T> repository, MessageHandler<T> sendHandler) {
        return SerialMessageSender.create(repository, sendHandler);
    }

    void sendToAll(Object message);

    void sendTo(Object message, InetSocketAddress[] userEndPoints);
}
