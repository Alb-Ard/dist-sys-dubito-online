package org.albard.dubito.serialization.json;

import java.io.InputStream;
import java.util.Optional;

import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.utils.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonObjectSerializer<X> implements ObjectSerializer<X> {
    private final ObjectMapper jsonMapper;

    public JsonObjectSerializer() {
        this.jsonMapper = new ObjectMapper();
        // Since we don't want to close the stream provided to deserialize, disable
        // AUTO_CLOSE feature
        this.jsonMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        DefaultJsonMapping.applyDefaultMapping(this.jsonMapper);
    }

    @Override
    public byte[] serialize(final X data) {
        try {
            // Wrap the object in an array, so that jackson treats it as a non-top-level
            // json object and writes the default typing information to the json
            final String json = this.jsonMapper.writeValueAsString(new Object[] { data });
            return json.getBytes("UTF-8");
        } catch (final Exception ex) {
            Logger.logError("Could not serialize JSON: " + ex.getMessage());
            return new byte[0];
        }
    }

    @Override
    public <Y extends X> Optional<Y> deserialize(final InputStream data, final Class<Y> dataClass) {
        try {
            try {
                // Read as an object array (to reflect the behaviour of serialize())
                return Optional.ofNullable(dataClass.cast(this.jsonMapper.readValue(data, Object[].class)[0]));
            } catch (final Exception ex) {
                Logger.logError("Could not deserialize JSON: " + ex.getMessage());
            }
            return Optional.empty();
        } catch (final Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}
