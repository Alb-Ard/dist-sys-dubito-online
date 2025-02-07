package org.albard.dubito.app.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private final Set<MessageHandler> messageListeners = Collections.synchronizedSet(new HashSet<>());

    private volatile boolean isStarted = false;

    public HashMapMessageDispatcher(final PeerId localPeerId) {
        this.localPeerId = localPeerId;

    }

    public void addPeer(final PeerId key, final MessageSender sender, final MessageReceiver receiver) {
        this.senders.putIfAbsent(key, sender);
        if (this.receivers.putIfAbsent(key, receiver) == null) {
            receiver.addMessageListener(this::handleMessageFromPeer);
            if (this.isStarted) {
                receiver.start();
            }
        }
    }

    private boolean handleMessageFromPeer(final GameMessage message) {
        // Protection against loops
        if (message.getSender() == localPeerId) {
            return false;
        }
        return this.messageListeners.stream().map(l -> l.handleMessage(message)).anyMatch(h -> h);
    }

    public void removePeer(final PeerId key) {
        this.senders.remove(key);
        final MessageReceiver receiver = this.receivers.remove(key);
        if (receiver != null) {
            receiver.removeMessageListener(this::handleMessageFromPeer);
        }
    }

    public int getPeerCount() {
        return this.receivers.size();
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
        receipients.stream().map(this.senders::get).filter(s -> s != null).forEach(s -> s.sendMessage(message));
    }
}
