package org.albard.dubito.app.messaging;

import org.albard.dubito.app.messaging.messages.GameMessage;

public interface MessageSerializer {
    byte[] serialize(GameMessage message);

    GameMessage deserialize(byte[] message);
}
