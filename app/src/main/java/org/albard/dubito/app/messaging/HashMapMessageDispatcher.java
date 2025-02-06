package org.albard.dubito.app.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.albard.dubito.app.messaging.handlers.MessageHandler;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.network.PeerId;

public final class HashMapMessageDispatcher implements MessageDispatcher {
    private final Map<PeerId, MessageSender> senders = Collections.synchronizedMap(new HashMap<>());
    private final Map<PeerId, MessageReceiver> receivers = Collections.synchronizedMap(new HashMap<>());
    private final PeerId localPeerId;

    private volatile boolean isStarted = false;
    private volatile MessageHandler messageListener;

    public HashMapMessageDispatcher(final PeerId localPeerId) {
        this.localPeerId = localPeerId;

    }

    public void addPeer(final PeerId key, final MessageSender sender, final MessageReceiver receiver) {
        this.senders.putIfAbsent(key, sender);
        if (this.receivers.putIfAbsent(key, receiver) == null) {
            receiver.setMessageListener(m -> {
                // Protection against loops
                if (m.getSender() == localPeerId) {
                    return false;
                }
                if (this.messageListener != null) {
                    return this.messageListener.handleMessage(m);
                }
                return false;
            });
            if (this.isStarted) {
                receiver.start();
            }
        }
    }

    public void removePeer(final PeerId key) {
        this.senders.remove(key);
        final MessageReceiver receiver = this.receivers.remove(key);
        if (receiver != null) {
            receiver.setMessageListener(null);
        }
    }

    public int getPeerCount() {
        return this.receivers.size();
    }

    @Override
    public void setMessageListener(final MessageHandler listener) {
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
        Set<PeerId> receipients = message.getReceipients();
        if (receipients == null || receipients.isEmpty()) {
            receipients = this.senders.keySet();
        }
        receipients = receipients.stream().filter(s -> message.getSender() != s).collect(Collectors.toSet());
        System.out.println("Sending " + message.getClass().getName() + " to " + receipients);
        receipients.stream().map(this.senders::get).forEach(s -> s.sendMessage(message));
    }
}
