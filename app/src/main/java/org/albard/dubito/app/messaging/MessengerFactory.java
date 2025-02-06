package org.albard.dubito.app.messaging;

import java.io.IOException;
import java.net.Socket;

public class MessengerFactory {
    private final MessageSerializer messageSerializer;

    public MessengerFactory(final MessageSerializer messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    public MessageSender createSender(final Socket socket) throws IOException {
        return MessageSender.createFromStream(socket.getOutputStream(), this.messageSerializer::serialize);
    }

    public MessageReceiver createReceiver(final Socket socket) throws IOException {
        return MessageReceiver.createFromStream(socket.getInputStream(), this.messageSerializer::deserialize);
    }
}
