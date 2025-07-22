package org.albard.dubito.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.albard.dubito.messaging.Messenger;
import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.utils.ObservableCloseable;

public interface PeerConnection extends Closeable, ObservableCloseable, Messenger {
    static PeerConnection createAndConnect(String bindAddress, int bindPort, String remoteAddress, int remotePort,
            final MessengerFactory messengerFactory) throws IOException {
        final Socket socket = new Socket();
        socket.bind(new InetSocketAddress(bindAddress, bindPort));
        socket.connect(new InetSocketAddress(remoteAddress, remotePort));
        return TcpPeerConnection.createConnected(socket, messengerFactory);
    }

    PeerEndPoint getRemoteEndPoint();
}
