package org.albard.dubito.app;

public interface MessageSender<T> {
    @FunctionalInterface
    public interface MessageHandler<T> {
        void send(T user, Object message);
    }

    static <T> MessageSender<T> create(UserConnectionRepository<T> repository, MessageHandler<T> sendHandler) {
        return SerialMessageSender.create(repository, sendHandler);
    }

    void sendToAll(Object message);
}
