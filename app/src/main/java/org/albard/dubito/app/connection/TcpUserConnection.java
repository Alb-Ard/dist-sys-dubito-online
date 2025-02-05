package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public final class TcpUserConnection implements UserConnection {
    private final Socket socket;

    private TcpUserConnection(final Socket socket) {
        this.socket = socket;
    }

    public static TcpUserConnection createConnected(final Socket socket) throws SocketException {
        if (!socket.isConnected()) {
            throw new SocketException("Socket is not connected");
        }
        return new TcpUserConnection(socket);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }
}
