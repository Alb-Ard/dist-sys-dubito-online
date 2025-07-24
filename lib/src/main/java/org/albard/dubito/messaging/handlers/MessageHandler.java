package org.albard.dubito.messaging.handlers;

import org.albard.dubito.messaging.messages.GameMessage;

@FunctionalInterface
public interface MessageHandler {
    boolean handleMessage(GameMessage message);
}
