package org.albard.dubito.app.messaging;

import org.albard.dubito.app.messaging.messages.GameMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface MessageSerializer {
    static MessageSerializer createJson() {
        return new MessageSerializer() {
            private final ObjectMapper jsonMapper = new ObjectMapper();

            @Override
            public byte[] serialize(final GameMessage message) {
                try {
                    return this.jsonMapper.writeValueAsBytes(this.jsonMapper.createObjectNode()
                            .put("className", message.getClass().getName()).putPOJO("messageBody", message));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    return new byte[0];
                }
            }

            @Override
            public GameMessage deserialize(final byte[] rawMessage) {
                try {
                    final JsonNode jsonRoot = this.jsonMapper.readTree(rawMessage);
                    final String className = jsonRoot.get("className").asText();
                    final Class<?> messageClass = GameMessage.class.getClassLoader().loadClass(className);
                    return (GameMessage) this.jsonMapper.treeToValue(jsonRoot.get("messageBody"), messageClass);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        };
    }

    byte[] serialize(GameMessage message);

    GameMessage deserialize(byte[] message);
}
