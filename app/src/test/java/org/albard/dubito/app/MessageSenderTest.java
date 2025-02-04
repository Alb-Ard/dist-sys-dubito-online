package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MessageSenderTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(() -> MessageSender.create(UserConnectionRepository.createEmpty(), (a, b) -> {
        }));
    }

    @Test
    void testSend() {
        final int userCount = 10;
        final List<Boolean> wereMessagesSent = new LinkedList<>();
        final UserConnectionRepository<Consumer<Object>> repository = UserConnectionRepository.createEmpty();
        for (int i = 0; i < userCount; i++) {
            repository.addUser(new InetSocketAddress(i), v -> wereMessagesSent.add(true));
        }
        final MessageSender<Consumer<Object>> sender = MessageSender.create(repository, (u, m) -> u.accept(m));
        Assertions.assertDoesNotThrow(() -> sender.sendToAll("Test"));
        Assertions.assertEquals(wereMessagesSent.size(), userCount);
        Assertions.assertAll(wereMessagesSent.stream().map(v -> () -> Assertions.assertTrue(v)));
    }
}
