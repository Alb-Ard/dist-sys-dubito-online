package org.albard.dubito.serialization;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ObjectSerializer<T> {
    byte[] serialize(final T data);

    <Y extends T> Optional<Y> deserialize(final InputStream data, final Class<Y> dataClass);

    default <Y extends T> List<Y> deserializeAll(final InputStream data, final Class<Y> dataClass) {
        final List<Y> objects = new ArrayList<>();
        try {
            while (data.available() > 0) {
                this.deserialize(data, dataClass).ifPresent(objects::add);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return objects;
    }
}
