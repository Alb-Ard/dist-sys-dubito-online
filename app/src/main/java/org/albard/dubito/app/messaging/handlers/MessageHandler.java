package org.albard.dubito.app.messaging.handlers;

import java.net.InetSocketAddress;

public interface MessageHandler {
    boolean handleMessage(InetSocketAddress fromEndPoint, Object message);
}
