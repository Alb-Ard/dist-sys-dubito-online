package org.albard.dubito.app.messaging.handlers;

import java.util.Set;

import org.albard.dubito.app.messaging.MessageSender;
import org.albard.dubito.app.messaging.messages.GameMessage;
import org.albard.dubito.app.messaging.messages.PingMessage;
import org.albard.dubito.app.messaging.messages.PongMessage;
import org.albard.dubito.app.network.PeerId;

public final class PingPongMessageHandler implements MessageHandler {
    private final MessageSender pongMessageSender;
    private final PeerId localPeerId;

    public PingPongMessageHandler(final PeerId localPeerId, final MessageSender pongMessageSender) {
        this.localPeerId = localPeerId;
        this.pongMessageSender = pongMessageSender;
    }

    @Override
    public boolean handleMessage(final GameMessage message) {
        if (message instanceof PingMessage) {
            System.out.println("Received Ping from " + message.getSender() + ", sending Pong reply");
            pongMessageSender.sendMessage(new PongMessage(this.localPeerId, Set.of(message.getSender())));
            return true;
        }
        if (message instanceof PongMessage) {
            System.out.println("Received Pong from " + message.getSender());
            return true;
        }
        return false;
    }
}
