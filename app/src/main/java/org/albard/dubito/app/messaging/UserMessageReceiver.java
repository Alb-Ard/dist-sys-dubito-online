package org.albard.dubito.app.messaging;

import java.io.Closeable;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public interface UserMessageReceiver extends Closeable {
    static UserMessageReceiver createFromStream(final InputStream stream, final Function<byte[], Object> deserializer) {
        return BufferedMessageReceiver.createFromStream(stream, deserializer);
    }

    void setMessageListener(Consumer<Object> listener);

    void start();
}
