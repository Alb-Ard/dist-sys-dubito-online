package org.albard.dubito.app.messaging;

import java.net.InetSocketAddress;

import org.albard.dubito.app.UserConnectionRepository;

public interface UserMessageSender<T> extends MessageSender {
    @FunctionalInterface
    public interface MessageHandler<T> {
        void send(InetSocketAddress endPoint, T connection, Object message);
    }

    static <T> UserMessageSender<T> create(UserConnectionRepository<T> repository, MessageHandler<T> sendHandler) {
        return SerialMessageSender.create(repository, sendHandler);
    }
}
