package org.albard.dubito.app.messaging.handlers;

import org.albard.dubito.app.UserEndPoint;

public interface MessageHandler {
    boolean handleMessage(UserEndPoint fromEndPoint, Object message);
}
