package org.albard.dubito.app.messaging.handlers;

import org.albard.dubito.app.UserEndPoint;
import org.albard.dubito.app.messaging.messages.GameMessage;

public interface MessageHandler {
    boolean handleMessage(UserEndPoint fromEndPoint, GameMessage message);
}
