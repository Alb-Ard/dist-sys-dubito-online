package org.albard.dubito.serialization.json;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;

import org.albard.dubito.serialization.ObjectSerializer;
import org.albard.utils.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonObjectSerializer<X> implements ObjectSerializer<X> {
    private static final String CHARSET_NAME = "UTF-8";
    private static final String SEPARATOR = "\n\n\n";

    private final ObjectMapper jsonMapper;

    public JsonObjectSerializer() {
        this.jsonMapper = new ObjectMapper();
        DefaultJsonConfiguration.apply(this.jsonMapper);
    }

    @Override
    public byte[] serialize(final X data) {
        try {
            // Wrap the object in an array, so that Jackson treats it as a non-top-level
            // Json object and writes the default typing information to the Json
            final String json = this.jsonMapper.writeValueAsString(new Object[] { data }) + SEPARATOR;
            return json.getBytes(CHARSET_NAME);
        } catch (final Exception ex) {
            Logger.logError("Could not serialize JSON: " + ex.getMessage());
            return new byte[0];
        }
    }

    @Override
    public <Y extends X> Optional<Y> deserialize(final InputStream data, final Class<Y> dataClass) {
        // Since I need to read until I find the SEPARATOR, I need to read the input
        // char-by-char.
        // However, I can't use an InputStreamReader, since it may (will) read ahead,
        // causing consecutive calls to deserialize to miss some bytes.
        // So I manually read bytes and store them in a separate stream, until I find
        // the SEPARATOR, then I use the string in that buffer as JSON.
        // Since the call to data.read() is blocking until end-of-stream is reached or
        // data is available, I can mimic the base behavior of ObjectMapper.readValue by
        // checking if it returns -1 and returning an error.
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream(128)) {
            final StringBuilder buffer = new StringBuilder(128);
            Logger.logDebug("Reading object " + dataClass.getSimpleName());
            while (true) {
                final int nextByte = data.read();
                if (nextByte < 0) {
                    Logger.logError("Could not deserialize JSON: End of Stream reached");
                    return Optional.empty();
                }
                output.write(nextByte);
                buffer.setLength(0);
                buffer.append(output.toString());
                if (buffer.indexOf(SEPARATOR) >= 0) {
                    Logger.logTrace("Found separator");
                    break;
                }
            }
            // Read as an object array (to reflect the behavior of serialize())
            final String json = buffer.toString().replace(SEPARATOR, "");
            Logger.logDebug("Deserializing JSON " + json);
            return Optional.ofNullable(dataClass.cast(this.jsonMapper.readValue(json, Object[].class)[0]));
        } catch (final Exception ex) {
            Logger.logError("Could not deserialize JSON: " + ex.getMessage());
        }
        return Optional.empty();
    }
}
