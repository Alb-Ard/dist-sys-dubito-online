package org.albard.dubito.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserEndPointPairTest {
    @Test
    void testCreateFromPair() {
        final PeerEndPointPair pair = new PeerEndPointPair(TestUtilities.createMockEndPoint(1),
                TestUtilities.createMockEndPoint(2));
        Assertions.assertNotNull(pair);
        Assertions.assertEquals(TestUtilities.createMockEndPoint(1), pair.getLocalEndPoint());
        Assertions.assertEquals(TestUtilities.createMockEndPoint(2), pair.getRemoteEndPoint());
    }

    @Test
    void testCreateFromSocket() throws UnknownHostException, IOException {
        try (final ServerSocket server = TestUtilities.createAndLaunchServer("127.0.0.1", 9001);
                final Socket client = new Socket()) {
            client.bind(new InetSocketAddress("127.0.0.1", 50000));
            client.connect(new InetSocketAddress("127.0.0.1", 9001));
            final PeerEndPointPair pair = PeerEndPointPair.createFromSocket(client);
            Assertions.assertNotNull(pair);
            Assertions.assertEquals(TestUtilities.createMockEndPoint(50000), pair.getLocalEndPoint());
            Assertions.assertEquals(TestUtilities.createMockEndPoint(9001), pair.getRemoteEndPoint());
        }
    }

    @Test
    void testReverse() {
        final PeerEndPointPair pair = new PeerEndPointPair(TestUtilities.createMockEndPoint(1),
                TestUtilities.createMockEndPoint(2)).getReversed();
        Assertions.assertEquals(TestUtilities.createMockEndPoint(2), pair.getLocalEndPoint());
        Assertions.assertEquals(TestUtilities.createMockEndPoint(1), pair.getRemoteEndPoint());
    }
}
