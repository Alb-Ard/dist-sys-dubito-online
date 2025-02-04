package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.albard.dubito.app.UserConnectionRepository;

public interface UserConnectionReceiver extends Closeable {
    public static UserConnectionReceiver createBound(UserConnectionRepository<Socket> repository, String bindAddress,
            int bindPort) throws UnknownHostException, IOException {
        return TcpUserConnectionReceiver.createAndBind(repository, bindAddress, bindPort);
    }

    public void start() throws IOException;

    public boolean isListening();

    public int getUserCount();
}
