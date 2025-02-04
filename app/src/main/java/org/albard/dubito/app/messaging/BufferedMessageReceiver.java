package org.albard.dubito.app.messaging;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BufferedMessageReceiver implements UserMessageReceiver {
    private final Thread receiveThread;

    private Consumer<Object> messageListener;
    private volatile boolean isRunning = false;

    public BufferedMessageReceiver(final InputStream stream, final Function<byte[], Object> deserializer) {
        this.receiveThread = Thread.ofVirtual().unstarted(() -> {
            final BufferedInputStream bufferedStream = new BufferedInputStream(stream);
            final byte[] buffer = new byte[1024];
            this.isRunning = true;
            while (this.isRunning) {
                try {
                    final int readByteCount = bufferedStream.read(buffer);
                    if (readByteCount <= 0) {
                        this.isRunning = false;
                    }
                    final byte[] messageBuffer = new byte[readByteCount];
                    System.arraycopy(buffer, 0, messageBuffer, 0, readByteCount);
                    this.messageListener.accept(deserializer.apply(messageBuffer));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    static UserMessageReceiver createFromStream(final InputStream stream, final Function<byte[], Object> deserializer) {
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

    @Override
    public void close() throws IOException {
        this.isRunning = false;
        try {
            this.receiveThread.join();
        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
