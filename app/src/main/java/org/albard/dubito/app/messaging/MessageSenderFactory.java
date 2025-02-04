package org.albard.dubito.app.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public final class MessageSenderFactory {
    private static class SocketWriter implements Consumer<Object> {
        private final MessageSerializer<Socket> messageSerializer;
        private final OutputStream stream;
        private final Socket socket;

        public SocketWriter(final Socket connection, final MessageSerializer<Socket> messageSerializer)
                throws IOException {
            this.messageSerializer = messageSerializer;
            this.socket = connection;
            this.stream = connection.getOutputStream();
        }

        @Override
        public void accept(Object message) {
            if (this.stream == null) {

            }
            try {
                this.stream.write(messageSerializer.serialize((InetSocketAddress) this.socket.getRemoteSocketAddress(),
                        this.socket, message));
                this.stream.flush();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public MessageSender create(Consumer<Object> sendHandler) {
        return MessageSender.create(sendHandler);
    }

    public MessageSender createSocketSender(final Socket socket, final MessageSerializer<Socket> messageSerializer)
            throws IOException {
        return this.create(new SocketWriter(socket, messageSerializer));
    }
}
