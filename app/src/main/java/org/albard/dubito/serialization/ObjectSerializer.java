package org.albard.dubito.serialization;

import java.io.InputStream;
import java.util.Optional;

public interface ObjectSerializer<T> {
    byte[] serialize(final T data);

    <Y extends T> Optional<Y> deserialize(final InputStream data, final Class<Y> dataClass);
}
