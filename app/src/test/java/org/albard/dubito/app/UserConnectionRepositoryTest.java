package org.albard.dubito.app;

import java.net.Socket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserConnectionRepositoryTest {
    @Test
    void testAddNewUser() {
        UserConnectionRepository repository = UserConnectionRepository.create();
        Socket userSocket = new Socket();
        Assertions.assertTrue(repository.addUser(userSocket));
    }
}
