package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public interface UserConnectionReceiver extends Closeable {
    public static UserConnectionReceiver createBound(String bindAddress, int bindPort)
            throws UnknownHostException, IOException {
        return TcpUserConnectionReceiver.createBound(bindAddress, bindPort);
    }

    public void start() throws IOException;

    public boolean isListening();

    public void setUserConnectedListener(Consumer<UserConnection> listener);
}
