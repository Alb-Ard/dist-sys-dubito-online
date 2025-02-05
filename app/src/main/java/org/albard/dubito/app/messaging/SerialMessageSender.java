package org.albard.dubito.app.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

public final class SerialMessageSender implements MessageSender {
    private final OutputStream stream;
    private final Function<Object, byte[]> serializer;

    public SerialMessageSender(final OutputStream stream, final Function<Object, byte[]> serializer) {
        this.stream = stream;
        this.serializer = serializer;
    }

    static MessageSender createFromStream(final OutputStream stream, final Function<Object, byte[]> serializer) {
        return new SerialMessageSender(stream, serializer);
    }

    @Override
    public void send(final Object message) {
        try {
            this.stream.write(this.serializer.apply(message));
            this.stream.flush();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
