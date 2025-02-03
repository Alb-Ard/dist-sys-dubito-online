package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class TcpUserConnectionSender implements UserConnectionSender {
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
