package org.albard.dubito.app;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserEndPointTest {
    @Test
    void testCreateValid() {
        final UserEndPoint address = UserEndPoint.createFromValues("127.0.0.1", 1);
        Assertions.assertNotNull(address);
        Assertions.assertEquals("127.0.0.1", address.getHost());
        Assertions.assertEquals(1, address.getPort());
    }

    @Test
    void testCreateWithInvalidAddress() {
        final UserEndPoint address = UserEndPoint.createFromValues("Abcd", 1);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithEmptyAddress() {
        final UserEndPoint address = UserEndPoint.createFromValues("", 1);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithNullAddress() {
        final UserEndPoint address = UserEndPoint.createFromValues(null, 1);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithZeroPort() {
        final UserEndPoint address = UserEndPoint.createFromValues("127.0.0.1", 0);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateWithNegativePort() {
        final UserEndPoint address = UserEndPoint.createFromValues("127.0.0.1", -10);
        Assertions.assertNull(address);
    }

    @Test
    void testCreateFromSocket() {
        final UserEndPoint address = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 10));
        Assertions.assertNotNull(address);
        Assertions.assertEquals("192.168.1.1", address.getHost());
        Assertions.assertEquals(10, address.getPort());
    }

    @Test
    void testBroadcast() {
        final UserEndPoint address = UserEndPoint.BROADCAST;
        Assertions.assertNotNull(address);
        Assertions.assertEquals("*", address.getHost());
        Assertions.assertEquals(0, address.getPort());
        Assertions.assertEquals("BROADCAST", address.toString());
    }

    @Test
    void testEquality() {
        final UserEndPoint a = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 10));
        final UserEndPoint b = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 10));
        Assertions.assertTrue(a.equals(b));
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHostInequality() {
        final UserEndPoint a = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 10));
        final UserEndPoint b = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.0.1", 10));
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testPortInequality() {
        final UserEndPoint a = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 10));
        final UserEndPoint b = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 20));
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToString() {
        final UserEndPoint address = UserEndPoint.createFromAddress(new InetSocketAddress("192.168.1.1", 10));
        Assertions.assertEquals("192.168.1.1:10", address.toString());
    }
}
