package org.albard.dubito.app.messaging.handlers;

import org.albard.dubito.app.messaging.messages.GameMessage;

@FunctionalInterface
public interface MessageHandler {
    boolean handleMessage(GameMessage message);
}
