package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public interface PeerConnection extends Closeable {
    static PeerConnection createAndConnect(String bindAddress, int bindPort, String remoteAddress, int remotePort)
            throws IOException {
        final Socket socket = new Socket();
        socket.bind(new InetSocketAddress(bindAddress, bindPort));
        socket.connect(new InetSocketAddress(remoteAddress, remotePort));
        return TcpPeerConnection.createConnected(socket);
    }

    Socket getSocket();
}
