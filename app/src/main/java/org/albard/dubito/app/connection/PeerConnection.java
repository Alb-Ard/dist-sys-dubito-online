package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.albard.dubito.app.messaging.MessageReceiver;
import org.albard.dubito.app.messaging.MessageSender;
import org.albard.dubito.app.messaging.MessengerFactory;

public interface PeerConnection extends Closeable, MessageSender, MessageReceiver {
    static PeerConnection createAndConnect(String bindAddress, int bindPort, String remoteAddress, int remotePort,
            final MessengerFactory messengerFactory) throws IOException {
        final Socket socket = new Socket();
        socket.bind(new InetSocketAddress(bindAddress, bindPort));
        socket.connect(new InetSocketAddress(remoteAddress, remotePort));
        return TcpPeerConnection.createConnected(socket, messengerFactory);
    }

    Socket getSocket();
}
