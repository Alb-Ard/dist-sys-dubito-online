package org.albard.dubito.app.messaging;

import java.io.InputStream;
import java.net.SocketException;
import java.util.function.Function;

import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class BufferedMessageReceiver implements MessageReceiver {
    private final Thread receiveThread;

    private volatile MessageHandler messageListener;

    public BufferedMessageReceiver(final InputStream stream, final Function<byte[], GameMessage> deserializer) {
        this.receiveThread = Thread.ofVirtual().unstarted(() -> {
            final byte[] buffer = new byte[1024];
            while (true) {
                try {
                    final int readByteCount = stream.read(buffer);
                    if (readByteCount <= 0) {
                        break;
                    }
                    final byte[] messageBuffer = new byte[readByteCount];
                    System.arraycopy(buffer, 0, messageBuffer, 0, readByteCount);
                    this.messageListener.handleMessage(deserializer.apply(messageBuffer));
                } catch (final SocketException ex) {
                    ex.printStackTrace();
                    break;
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    static MessageReceiver createFromStream(final InputStream stream,
            final Function<byte[], GameMessage> deserializer) {
        return new BufferedMessageReceiver(stream, deserializer);
    }

    @Override
    public void start() {
        this.receiveThread.start();
    }

    @Override
    public void setMessageListener(final MessageHandler listener) {
        this.messageListener = listener;
    }
}
