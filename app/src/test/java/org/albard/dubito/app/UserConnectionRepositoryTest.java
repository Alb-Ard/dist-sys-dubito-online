package org.albard.dubito.app;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionRepositoryTest {
    @Test
    void testCreatesEmpty() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testAddNewUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        Assertions.assertTrue(repository.addUser(new InetSocketAddress("127.0.0.1", 5050), new Object()));
        Assertions.assertEquals(repository.getUserCount(), 1);
    }

    @Test
    void testAddExistingUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        final Object value = new Object();
        repository.addUser(new InetSocketAddress("127.0.0.1", 5050), value);
        Assertions.assertFalse(repository.addUser(new InetSocketAddress("127.0.0.1", 5050), value));
        Assertions.assertEquals(repository.getUserCount(), 1);
        Assertions.assertEquals(repository.getUser(new InetSocketAddress("127.0.0.1", 5050)), value);
    }

    @Test
    void testRemoveExistingUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        repository.addUser(new InetSocketAddress("127.0.0.1", 5050), new Object());
        Assertions.assertTrue(repository.removeUser(new InetSocketAddress("127.0.0.1", 5050)));
        Assertions.assertEquals(repository.getUserCount(), 0);
        Assertions.assertEquals(repository.getUser(new InetSocketAddress("127.0.0.1", 5050)), null);
    }

    @Test
    void testRemoveNonExistingUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        Assertions.assertFalse(repository.removeUser(new InetSocketAddress("127.0.0.1", 5050)));
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testRemoveAlreadyRemovedUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint, new Object());
        Assertions.assertTrue(repository.removeUser(endPoint));
        Assertions.assertFalse(repository.removeUser(endPoint));
        Assertions.assertEquals(repository.getUserCount(), 0);
    }

    @Test
    void testClear() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        for (int i = 0; i < 10; i++) {
            final Object value = new Object();
            repository.addUser(new InetSocketAddress("127.0.0.1", i), value);
            Assertions.assertEquals(repository.getUser(new InetSocketAddress("127.0.0.1", i)), value);
        }
        repository.clear();
        Assertions.assertEquals(repository.getUserCount(), 0);
    }
}
