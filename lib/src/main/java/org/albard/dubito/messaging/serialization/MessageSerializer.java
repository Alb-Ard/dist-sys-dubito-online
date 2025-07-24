package org.albard.dubito.messaging.serialization;

import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.dubito.serialization.json.JsonObjectSerializer;

public final class MessageSerializer {
    public static ObjectSerializer<GameMessage> createJson() {
        return new JsonObjectSerializer<>();
    }
}
