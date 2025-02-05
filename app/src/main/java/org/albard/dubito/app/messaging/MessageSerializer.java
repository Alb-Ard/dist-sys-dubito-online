package org.albard.dubito.app.messaging;

public interface MessageSerializer<T> {
    byte[] serialize(Object message);

    Object deserialize(byte[] message);
}
