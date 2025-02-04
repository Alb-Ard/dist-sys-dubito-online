package org.albard.dubito.app.messaging;

import java.net.InetSocketAddress;

public interface MessageSerializer<T> {
    byte[] serialize(InetSocketAddress user, T connection, Object message);

    Object deserialize(InetSocketAddress user, T connection, byte[] message);
}
