package org.albard.dubito.app.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.albard.dubito.app.UserEndPoint;
import org.albard.dubito.app.messaging.messages.GameMessage;

public final class MessageDispatcher implements MessageSender, MessageReceiver {
    private final Map<UserEndPoint, MessageSender> senders = Collections.synchronizedMap(new HashMap<>());
    private final Map<UserEndPoint, MessageReceiver> receivers = Collections.synchronizedMap(new HashMap<>());

    private volatile boolean isStarted = false;
    private volatile Consumer<GameMessage> messageListener;

    public void addMessenger(final UserEndPoint key, final MessageSender sender, final MessageReceiver receiver) {
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

    public void removeMessenger(final UserEndPoint key) {
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
    public void setMessageListener(final Consumer<GameMessage> listener) {
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
    public void sendMessage(final GameMessage message) {
        this.senders.values().forEach(s -> s.sendMessage(message));
    }
}
