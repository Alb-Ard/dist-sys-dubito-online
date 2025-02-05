package org.albard.dubito.app.messaging;

public interface MessageSerializer {
    byte[] serialize(Object message);

    Object deserialize(byte[] message);
}
