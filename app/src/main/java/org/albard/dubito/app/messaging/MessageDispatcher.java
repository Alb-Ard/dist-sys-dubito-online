package org.albard.dubito.app.messaging;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MessageDispatcher implements MessageSender, MessageReceiver {
    private final Map<InetSocketAddress, MessageSender> senders = Collections.synchronizedMap(new HashMap<>());
    private final Map<InetSocketAddress, MessageReceiver> receivers = Collections.synchronizedMap(new HashMap<>());

    private volatile boolean isStarted = false;
    private volatile Consumer<Object> messageListener;

    public void addMessenger(final InetSocketAddress key, final MessageSender sender, final MessageReceiver receiver) {
        this.senders.putIfAbsent(key, sender);
        if (this.receivers.putIfAbsent(key, receiver) == null) {
            receiver.setMessageListener(m -> {
                if (this.messageListener != null) {
                    this.messageListener.accept(m);
                }
            });
            if (this.isStarted) {
                receiver.start();
            }
        }
    }

    public void removeMessenger(final InetSocketAddress key) {
        this.senders.remove(key);
        final MessageReceiver receiver = this.receivers.remove(key);
        if (receiver != null) {
            receiver.setMessageListener(null);
        }
    }

    public int getMessengerCount() {
        return this.receivers.size();
    }

    @Override
    public void setMessageListener(final Consumer<Object> listener) {
        this.messageListener = listener;
    }

    @Override
    public void start() {
        if (this.isStarted) {
            return;
        }
        this.isStarted = true;
        this.receivers.values().forEach(r -> r.start());
    }

    @Override
    public void send(final Object message) {
        this.senders.values().forEach(s -> s.send(message));
    }
}
