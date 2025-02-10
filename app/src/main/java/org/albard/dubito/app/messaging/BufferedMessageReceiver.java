package org.albard.dubito.app.messaging;

import java.io.InputStream;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class BufferedMessageReceiver implements MessageReceiver {
    private final Thread receiveThread;
    private final Set<MessageHandler> messageListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<ReceiverClosedListener> closedListeners = Collections.synchronizedSet(new HashSet<>());

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
                    final GameMessage message = deserializer.apply(messageBuffer);
                    this.messageListeners.forEach(l -> l.handleMessage(message));
                } catch (final SocketException ex) {
                    System.err.println(ex.getMessage());
                    break;
                } catch (final Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
            this.closedListeners.forEach(l -> l.receiverClosed());
        });
    }

    static MessageReceiver createFromStream(final InputStream stream,
            final Function<byte[], GameMessage> deserializer) {
        final BufferedMessageReceiver receiver = new BufferedMessageReceiver(stream, deserializer);
        receiver.start();
        return receiver;
    }

    @Override
    public void addMessageListener(final MessageHandler listener) {
        this.messageListeners.add(listener);
    }

    @Override
    public void removeMessageListener(final MessageHandler listener) {
        this.messageListeners.remove(listener);
    }

    @Override
    public void addClosedListener(final ReceiverClosedListener listener) {
        this.closedListeners.add(listener);
    }

    @Override
    public void removeClosedListener(final ReceiverClosedListener listener) {
        this.closedListeners.remove(listener);
    }

    private void start() {
        this.receiveThread.start();
    }
}
