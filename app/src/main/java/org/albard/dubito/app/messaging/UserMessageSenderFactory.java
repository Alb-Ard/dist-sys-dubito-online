package org.albard.dubito.app.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.albard.dubito.app.UserConnectionRepository;
import org.albard.dubito.app.messaging.UserMessageSender.MessageHandler;

public final class UserMessageSenderFactory {
    public <T> UserMessageSender<T> create(UserConnectionRepository<T> repository, MessageHandler<T> sendHandler) {
        return UserMessageSender.create(repository, sendHandler);
    }

    public UserMessageSender<Socket> createSocketSender(UserConnectionRepository<Socket> repository,
            MessageSerializer<Socket> messageSerializer) {
        return this.create(repository, (endPoint, connection, message) -> {
            OutputStream stream;
            try {
                stream = connection.getOutputStream();
                stream.write(messageSerializer.serialize(endPoint, connection, message));
                stream.flush();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        });
    }

}
