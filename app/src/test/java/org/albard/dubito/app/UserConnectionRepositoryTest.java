package org.albard.dubito.app;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionRepositoryTest {
    @Test
    void testAddNewUser() {
        UserConnectionRepository repository = UserConnectionRepository.create();
        InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertTrue(repository.addUser(endPoint));
    }

    @Test
    void testAddExistingUser() {
        UserConnectionRepository repository = UserConnectionRepository.create();
        InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertTrue(repository.addUser(endPoint));
        InetSocketAddress endPoint2 = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertFalse(repository.addUser(endPoint2));
    }
}
