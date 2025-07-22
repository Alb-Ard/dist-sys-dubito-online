package org.albard.dubito;

import java.net.InetSocketAddress;

import org.albard.dubito.network.PeerEndPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserEndPointTest {
    @Test
    void testCreateValid() {
        final PeerEndPoint address = PeerEndPoint.ofValues("127.0.0.1", 1);
        Assertions.assertNotNull(address);
        Assertions.assertEquals("127.0.0.1", address.getHost());
        Assertions.assertEquals(1, address.getPort());
    }

    @Test
    void testCreateWithInvalidAddress() {
        final PeerEndPoint address = PeerEndPoint.ofValues("1.2.3.400", 1);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithEmptyAddress() {
        final PeerEndPoint address = PeerEndPoint.ofValues("", 1);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithNullAddress() {
        final PeerEndPoint address = PeerEndPoint.ofValues(null, 1);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithZeroPort() {
        final PeerEndPoint address = PeerEndPoint.ofValues("127.0.0.1", 0);
        Assertions.assertNotNull(address);
    }

    @Test
    void testCreateWithNegativePort() {
        final PeerEndPoint address = PeerEndPoint.ofValues("127.0.0.1", -10);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateFromSocket() {
        final PeerEndPoint address = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 10));
        Assertions.assertNotNull(address);
        Assertions.assertEquals("192.168.1.1", address.getHost());
        Assertions.assertEquals(10, address.getPort());
    }

    @Test
    void testEquality() {
        final PeerEndPoint a = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 10));
        final PeerEndPoint b = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 10));
        Assertions.assertTrue(a.equals(b));
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHostInequality() {
        final PeerEndPoint a = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 10));
        final PeerEndPoint b = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.0.1", 10));
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testPortInequality() {
        final PeerEndPoint a = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 10));
        final PeerEndPoint b = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 20));
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToString() {
        final PeerEndPoint address = PeerEndPoint.ofAddress(new InetSocketAddress("192.168.1.1", 10));
        Assertions.assertEquals("192.168.1.1:10", address.toString());
    }
}
