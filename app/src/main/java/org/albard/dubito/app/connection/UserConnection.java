package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public interface UserConnection extends Closeable {
    static UserConnection create() {
        return new TcpUserConnection();
    }

    void connect(String remoteAddress, int remotePort) throws IOException;

    Socket getSocket();
}
