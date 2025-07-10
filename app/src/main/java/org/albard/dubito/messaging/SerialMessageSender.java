package org.albard.dubito.messaging;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.utils.Logger;

public final class SerialMessageSender implements MessageSender {
    private final OutputStream stream;
    private final Function<GameMessage, byte[]> serializer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
            executor.submit(() -> {
                try {
                    final var data = this.serializer.apply(message);
                    Logger.logTrace("Writing " + data.length + "b...");
                    this.stream.write(data);
                    this.stream.flush();
                    Logger.logTrace("Written " + data.length + "b");
                } catch (final Exception ex) {
                    Logger.logError("Could not send message: " + ex.getMessage());
                }
            }).get();
        } catch (final Exception ex) {
            Logger.logError("Error waiting in sendMessage: " + ex.getMessage());
        }
    }
}
