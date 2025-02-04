package org.albard.dubito.app.messaging;

import java.net.InetSocketAddress;

public interface MessageSender {
    void sendToAll(Object message);

    void sendTo(Object message, InetSocketAddress[] userEndPoints);
}
