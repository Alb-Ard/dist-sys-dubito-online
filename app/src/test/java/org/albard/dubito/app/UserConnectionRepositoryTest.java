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
        repository.addUser(endPoint);
        InetSocketAddress endPoint2 = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertFalse(repository.addUser(endPoint2));
    }

    @Test
    void testRemoveExistingUser() {
        UserConnectionRepository repository = UserConnectionRepository.create();
        InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint);
        InetSocketAddress endPoint2 = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertTrue(repository.removeUser(endPoint2));
    }

    @Test
    void testRemoveNonExistingUser() {
        UserConnectionRepository repository = UserConnectionRepository.create();
        InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertFalse(repository.removeUser(endPoint));
    }

    @Test
    void testRemoveAlreadyRemovedUser() {
        UserConnectionRepository repository = UserConnectionRepository.create();
        InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint);
        Assertions.assertTrue(repository.removeUser(endPoint));
        Assertions.assertFalse(repository.removeUser(endPoint));
    }
}
