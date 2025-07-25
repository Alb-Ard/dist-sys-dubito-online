package org.albard.dubito.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.albard.dubito.messaging.MessengerFactory;
import org.albard.dubito.network.PeerEndPoint;

public interface PeerConnectionReceiver extends Closeable {
    public static PeerConnectionReceiver createBound(String bindAddress, int bindPort,
            MessengerFactory messengerFactory) throws UnknownHostException, IOException {
        return TcpPeerConnectionReceiver.createBound(bindAddress, bindPort, messengerFactory);
    }

    public boolean isListening();

    public void setPeerConnectedListener(Consumer<PeerConnection> listener);

    public void setPeerDisconnectedListener(Consumer<PeerConnection> listener);

    public MessengerFactory getMessengerFactory();

    public PeerEndPoint getBindEndPoint();
}
