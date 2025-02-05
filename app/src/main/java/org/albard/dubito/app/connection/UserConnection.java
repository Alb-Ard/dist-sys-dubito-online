package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public interface UserConnection extends Closeable {
    static UserConnection createAndConnect(String remoteAddress, int remotePort) throws IOException {
        final Socket socket = new Socket(remoteAddress, remotePort);
        return TcpUserConnection.createConnected(socket);
    }

    Socket getSocket();
}
