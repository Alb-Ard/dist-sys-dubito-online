package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public final class TcpPeerConnection implements PeerConnection {
    private final Socket socket;

    private TcpPeerConnection(final Socket socket) {
        this.socket = socket;
    }

    public static TcpPeerConnection createConnected(final Socket socket) throws SocketException {
        if (!socket.isConnected()) {
            throw new SocketException("Socket is not connected");
        }
        return new TcpPeerConnection(socket);
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
