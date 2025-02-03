package org.albard.dubito.app;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

public interface UserConnectionReceiver extends Closeable {
    public static UserConnectionReceiver createBound(UserConnectionRepository repository, String bindAddress,
            int bindPort) throws UnknownHostException, IOException {
        return TcpUserConnectionReceiver.createAndBind(repository, bindAddress, bindPort);
    }

    public void start() throws IOException;

    public boolean isListening();

    public int getUserCount();
}
