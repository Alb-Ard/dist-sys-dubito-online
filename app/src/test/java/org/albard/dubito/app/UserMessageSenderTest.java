package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.albard.dubito.app.messaging.UserMessageSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class UserMessageSenderTest {
    @Test
    void testCreate() {
        Assertions.assertDoesNotThrow(
                () -> UserMessageSender.create(UserConnectionRepository.createEmpty(), (a, b, c) -> {
                }));
    }

    @Test
    void testSendToAll() {
        final int userCount = 10;
        final List<InetSocketAddress> receipients = new LinkedList<>();
        final List<Object> receivedMessages = new LinkedList<>();
        final UserConnectionRepository<BiConsumer<InetSocketAddress, Object>> repository = UserConnectionRepository
                .createEmpty();
        final List<InetSocketAddress> repositoryUsers = createTestUsers(userCount, repository, (u, m) -> {
            receipients.add(u);
            receivedMessages.add(m);
        });
        final UserMessageSender<BiConsumer<InetSocketAddress, Object>> sender = UserMessageSender.create(repository,
                (u, c, m) -> c.accept(u, m));
        Assertions.assertDoesNotThrow(() -> sender.sendToAll("Test"));
        Assertions.assertEquals(userCount, receipients.size());
        Assertions.assertEquals(userCount, receivedMessages.size());
        for (int i = 0; i < userCount; i++) {
            Assertions.assertEquals(receipients.get(i), repositoryUsers.get(i));
            Assertions.assertEquals("Test", receivedMessages.get(i));
        }
    }

    @Test
    void testSendTo() {
        final int userCount = 10;
        final List<InetSocketAddress> receipients = new LinkedList<>();
        final List<Object> receivedMessages = new LinkedList<>();
        final UserConnectionRepository<BiConsumer<InetSocketAddress, Object>> repository = UserConnectionRepository
                .createEmpty();
        final List<InetSocketAddress> repositoryUsers = createTestUsers(userCount, repository, (u, m) -> {
            receipients.add(u);
            receivedMessages.add(m);
        });
        final UserMessageSender<BiConsumer<InetSocketAddress, Object>> sender = UserMessageSender.create(repository,
                (u, c, m) -> c.accept(u, m));
        sender.sendTo("Test", new InetSocketAddress[] { repositoryUsers.get(0), repositoryUsers.get(2) });
        Assertions.assertEquals(2, receipients.size());
        Assertions.assertEquals(2, receivedMessages.size());
        Assertions.assertEquals(repositoryUsers.get(0), receipients.get(0));
        Assertions.assertEquals("Test", receivedMessages.get(0));
        Assertions.assertEquals(repositoryUsers.get(2), receipients.get(1));
        Assertions.assertEquals("Test", receivedMessages.get(0));
    }

    private List<InetSocketAddress> createTestUsers(final int count,
            final UserConnectionRepository<BiConsumer<InetSocketAddress, Object>> repository,
            final BiConsumer<InetSocketAddress, Object> messageHandler) {
        final List<InetSocketAddress> users = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            final InetSocketAddress user = new InetSocketAddress(i);
            users.add(user);
            repository.addUser(user, messageHandler);
        }
        return users;
    }
}
