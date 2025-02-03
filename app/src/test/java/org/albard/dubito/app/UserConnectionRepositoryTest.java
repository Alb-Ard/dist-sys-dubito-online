package org.albard.dubito.app;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionRepositoryTest {
    @Test
    void testCreatesEmpty() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testAddNewUser() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertTrue(repository.addUser(endPoint));
        Assertions.assertEquals(repository.getUserCount(), 1);
    }

    @Test
    void testAddExistingUser() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint);
        final InetSocketAddress endPoint2 = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertFalse(repository.addUser(endPoint2));
        Assertions.assertEquals(repository.getUserCount(), 1);
    }

    @Test
    void testRemoveExistingUser() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint);
        final InetSocketAddress endPoint2 = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertTrue(repository.removeUser(endPoint2));
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testRemoveNonExistingUser() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        Assertions.assertFalse(repository.removeUser(endPoint));
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testRemoveAlreadyRemovedUser() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint);
        Assertions.assertTrue(repository.removeUser(endPoint));
        Assertions.assertFalse(repository.removeUser(endPoint));
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testClear() {
        final UserConnectionRepository repository = UserConnectionRepository.create();
        for (int i = 0; i < 10; i++) {
            repository.addUser(new InetSocketAddress("127.0.0.1", i));
        }
        repository.clear();
        Assertions.assertEquals(repository.getUserCount(), 0);
    }
}
