package org.albard.dubito.app.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class TcpUserConnection implements UserConnection {
    private final Socket socket = new Socket();

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void connect(String remoteAddress, int remotePort) throws IOException {
        this.socket.connect(new InetSocketAddress(remoteAddress, remotePort));
    }
}
