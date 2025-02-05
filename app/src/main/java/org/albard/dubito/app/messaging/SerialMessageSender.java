package org.albard.dubito.app.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import org.albard.dubito.app.messaging.messages.GameMessage;

public final class SerialMessageSender implements MessageSender {
    private final OutputStream stream;
    private final Function<GameMessage, byte[]> serializer;

    private SerialMessageSender(final OutputStream stream, final Function<GameMessage, byte[]> serializer) {
        this.stream = stream;
        this.serializer = serializer;
    }

    public static MessageSender createFromStream(final OutputStream stream,
            final Function<GameMessage, byte[]> serializer) {
        return new SerialMessageSender(stream, serializer);
    }

    @Override
    public void sendMessage(final GameMessage message) {
        try {
            this.stream.write(this.serializer.apply(message));
            this.stream.flush();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
