package org.albard.dubito.serialization.json;

import java.io.InputStream;
import java.util.Optional;

import org.albard.dubito.serialization.ObjectSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonObjectSerializer<X> implements ObjectSerializer<X> {
    private final ObjectMapper jsonMapper;

    public JsonObjectSerializer() {
        this.jsonMapper = new ObjectMapper();
        DefaultJsonMapping.applyDefaultMapping(this.jsonMapper);
    }

    @Override
    public byte[] serialize(final X data) {
        try {
            final String json = this.jsonMapper.writeValueAsString(new Object[] { data });
            return json.getBytes("UTF-8");
        } catch (final Exception ex) {
            ex.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public <Y extends X> Optional<Y> deserialize(final InputStream data, final Class<Y> dataClass) {
        try {
            if (data.available() > 0) {
                try {
                    return Optional.ofNullable(dataClass.cast(this.jsonMapper.readValue(data, Object[].class)[0]));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            return Optional.empty();
        } catch (final Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}
