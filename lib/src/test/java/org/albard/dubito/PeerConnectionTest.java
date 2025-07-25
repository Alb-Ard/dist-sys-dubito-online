package org.albard.dubito;

import static org.albard.dubito.TestUtilities.withMultiCloseable;
import static org.albard.dubito.TestUtilities.withSocketServer;

import org.albard.dubito.connection.PeerConnection;
import org.junit.jupiter.api.Test;

public final class PeerConnectionTest {
    @Test
    void testCreateAndConnect() throws Exception {
        withSocketServer("127.0.0.1", 9000, server -> {
            withMultiCloseable(2, i -> PeerConnection.createAndConnect("127.0.0.1", 9001 + i, "127.0.0.1", 9000,
                    TestUtilities.createMessengerFactory()), connections -> {
                    });
        });
    }
}
