package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionRepositoryTest {
    @Test
    void testCreatesEmpty() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        Assertions.assertEquals(0, repository.getUserCount());
    }

    @Test
    void testAddNewUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        Assertions.assertTrue(repository.addUser(new InetSocketAddress("127.0.0.1", 5050), new Object()));
        Assertions.assertEquals(1, repository.getUserCount());
    }

    @Test
    void testAddNewUserListener() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        final List<InetSocketAddress> addedUsers = new LinkedList<>();
        final List<Object> addedConnections = new LinkedList<>();
        final Object value = new Object();
        repository.addUserListener(new UserRepositoryListener<>() {
            @Override
            public void handleUserAdded(final InetSocketAddress endPoint, final Object connection) {
                addedUsers.add(endPoint);
                addedConnections.add(connection);
            }

            @Override
            public void handleUserRemoved(final InetSocketAddress endPoint, final Object connection) {
                Assertions.fail();
            }
        });
        repository.addUser(new InetSocketAddress("127.0.0.1", 5050), value);
        Assertions.assertEquals(1, addedUsers.size());
        Assertions.assertEquals(1, addedConnections.size());
        Assertions.assertEquals(new InetSocketAddress("127.0.0.1", 5050), addedUsers.get(0));
        Assertions.assertEquals(value, addedConnections.get(0));
    }

    @Test
    void testAddExistingUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        final Object value = new Object();
        repository.addUser(new InetSocketAddress("127.0.0.1", 5050), value);
        Assertions.assertFalse(repository.addUser(new InetSocketAddress("127.0.0.1", 5050), value));
        Assertions.assertEquals(1, repository.getUserCount());
        Assertions.assertEquals(value, repository.getUser(new InetSocketAddress("127.0.0.1", 5050)));
    }

    @Test
    void testRemoveExistingUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        repository.addUser(new InetSocketAddress("127.0.0.1", 5050), new Object());
        Assertions.assertTrue(repository.removeUser(new InetSocketAddress("127.0.0.1", 5050)));
        Assertions.assertEquals(0, repository.getUserCount());
        Assertions.assertEquals(null, repository.getUser(new InetSocketAddress("127.0.0.1", 5050)));
    }

    @Test
    void testRemoveUserListener() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        final List<InetSocketAddress> addedUsers = new LinkedList<>();
        addedUsers.add(new InetSocketAddress("127.0.0.1", 5050));
        final List<Object> addedConnections = new LinkedList<>();
        final Object value = new Object();
        addedConnections.add(value);
        repository.addUserListener(new UserRepositoryListener<>() {
            @Override
            public void handleUserAdded(final InetSocketAddress endPoint, final Object connection) {
            }

            @Override
            public void handleUserRemoved(final InetSocketAddress endPoint, final Object connection) {
                addedUsers.remove(endPoint);
                addedConnections.remove(connection);
            }
        });
        repository.addUser(new InetSocketAddress("127.0.0.1", 5050), value);
        repository.removeUser(new InetSocketAddress("127.0.0.1", 5050));
        Assertions.assertEquals(0, addedUsers.size());
        Assertions.assertEquals(0, addedConnections.size());
    }

    @Test
    void testRemoveNonExistingUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        Assertions.assertFalse(repository.removeUser(new InetSocketAddress("127.0.0.1", 5050)));
        Assertions.assertEquals(0, repository.getUserCount());
    }

    @Test
    void testRemoveAlreadyRemovedUser() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        final InetSocketAddress endPoint = new InetSocketAddress("127.0.0.1", 5050);
        repository.addUser(endPoint, new Object());
        Assertions.assertTrue(repository.removeUser(endPoint));
        Assertions.assertFalse(repository.removeUser(endPoint));
        Assertions.assertEquals(0, repository.getUserCount());
    }

    @Test
    void testClear() {
        final UserConnectionRepository<Object> repository = UserConnectionRepository.createEmpty();
        for (int i = 0; i < 10; i++) {
            final Object value = new Object();
            repository.addUser(new InetSocketAddress("127.0.0.1", i), value);
            Assertions.assertEquals(value, repository.getUser(new InetSocketAddress("127.0.0.1", i)));
        }
        repository.clear();
        Assertions.assertEquals(0, repository.getUserCount());
    }
}
