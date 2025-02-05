package org.albard.dubito.app.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public interface PeerConnectionReceiver extends Closeable {
    public static PeerConnectionReceiver createBound(String bindAddress, int bindPort)
            throws UnknownHostException, IOException {
        return TcpPeerConnectionReceiver.createBound(bindAddress, bindPort);
    }

    public void start() throws IOException;

    public boolean isListening();

    public void setPeerConnectedListener(Consumer<PeerConnection> listener);
}
