package org.albard.dubito.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.albard.dubito.app.connection.PeerConnection;

public final class PeerIdExchanger {
    private final PeerId localPeerId;

    public PeerIdExchanger(final PeerId localPeerId) {
        this.localPeerId = localPeerId;
    }

    public PeerId exchangeIds(final PeerConnection connection) throws IOException {
        sendIdToConnection(localPeerId, connection);
        return waitIdFromConnection(connection);
    }

    private static void sendIdToConnection(final PeerId localPeerId, final PeerConnection connection)
            throws IOException {
        // Non blocking, since we may want to do a send-receive of peer ids
        Thread.ofVirtual().start(() -> {
            try {
                final Socket socket = connection.getSocket();
                final OutputStream stream = socket.getOutputStream();
                stream.write(localPeerId.getBytes());
                stream.flush();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static PeerId waitIdFromConnection(final PeerConnection connection) throws IOException {
        final Socket socket = connection.getSocket();
        final InputStream stream = socket.getInputStream();
        final PeerId id = PeerId.createFromBytes(stream.readNBytes(PeerId.LENGTH));
        // Clean the input buffer
        while (stream.available() > 0) {
            stream.read();
        }
        return id;
    }
}
