package org.albard.dubito.app;

import java.io.Closeable;
import java.io.IOException;

public interface UserConnectionSender extends Closeable {
    static UserConnectionSender create() {
        return new TcpUserConnectionSender();
    }

    void connect(String remoteAddress, int remotePort) throws IOException;
}
