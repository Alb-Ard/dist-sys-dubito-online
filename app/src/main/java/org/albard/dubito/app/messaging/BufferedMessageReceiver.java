package org.albard.dubito.app.messaging;

import java.io.InputStream;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BufferedMessageReceiver implements MessageReceiver {
    private final Thread receiveThread;

    private volatile Consumer<Object> messageListener;

    public BufferedMessageReceiver(final InputStream stream, final Function<byte[], Object> deserializer) {
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
                    this.messageListener.accept(deserializer.apply(messageBuffer));
                } catch (final SocketException ex) {
                    ex.printStackTrace();
                    break;
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    static MessageReceiver createFromStream(final InputStream stream, final Function<byte[], Object> deserializer) {
        return new BufferedMessageReceiver(stream, deserializer);
    }

    @Override
    public void start() {
        this.receiveThread.start();
    }

    @Override
    public void setMessageListener(final Consumer<Object> listener) {
        this.messageListener = listener;
    }
}
