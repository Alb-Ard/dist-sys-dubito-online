package org.albard.dubito.messaging;

import java.io.IOException;
import java.net.Socket;

import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.serialization.ObjectSerializer;

public class MessengerFactory {
    private final ObjectSerializer<GameMessage> messageSerializer;

    public MessengerFactory(final ObjectSerializer<GameMessage> messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    public MessageSender createSender(final Socket socket) throws IOException {
        return MessageSender.createFromStream(socket.getOutputStream(), this.messageSerializer::serialize);
    }

    public MessageReceiver createReceiver(final Socket socket) throws IOException {
        return MessageReceiver.createFromStream(socket.getInputStream(),
                x -> this.messageSerializer.deserialize(x, GameMessage.class));
    }
}
