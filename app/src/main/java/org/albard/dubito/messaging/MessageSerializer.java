package org.albard.dubito.messaging;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.albard.dubito.messaging.messages.GameMessage;

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
            public List<GameMessage> deserialize(final byte[] rawMessage) {
                try {
                    // A rawMessage may contain multiple messages, so wrap the array in a stream and
                    // parse until the stream is empty
                    final ByteArrayInputStream stream = new ByteArrayInputStream(rawMessage);
                    final List<GameMessage> messages = new ArrayList<>();
                    while (stream.available() > 0) {
                        try {
                            final JsonNode jsonRoot = this.jsonMapper.readTree(stream);
                            final String className = jsonRoot.get("className").asText();
                            final Class<?> messageClass = GameMessage.class.getClassLoader().loadClass(className);
                            messages.add((GameMessage) this.jsonMapper.treeToValue(jsonRoot.get("messageBody"),
                                    messageClass));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return messages;
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    return List.of();
                }
            }
        };
    }

    byte[] serialize(GameMessage message);

    List<GameMessage> deserialize(byte[] message);
}
