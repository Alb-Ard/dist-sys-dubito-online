package org.albard.dubito.userManagement.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.User;
import org.albard.dubito.userManagement.messages.UpdateUserMessage;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.dubito.utils.Locked;

public final class UserClient {
    private final PeerNetwork network;
    private final Locked<Map<PeerId, User>> users = Locked.of(new HashMap<>());
    private final Set<Consumer<Set<User>>> userListChangedListeners = Collections.synchronizedSet(new HashSet<>());

    public UserClient(PeerNetwork network) {
        this.network = network;
        this.network.addMessageListener(this::handleMessage);
    }

    public void requestSetName(final String newName) {
        this.network.sendMessage(new UpdateUserMessage(this.network.getLocalPeerId(), null, newName));
    }

    public int getUserCount() {
        return this.users.getValue().size();
    }

    public User getLocalUser() {
        return this.getUser(this.network.getLocalPeerId());
    }

    public User getUser(final PeerId id) {
        return this.users.getValue().get(id);
    }

    public Set<User> getUsers() {
        return Set.copyOf(this.users.getValue().values());
    }

    public void addUserListChangedListener(final Consumer<Set<User>> listener) {
        this.userListChangedListeners.add(listener);
    }

    public void removeUserListChangedListener(final Consumer<Set<User>> listener) {
        this.userListChangedListeners.remove(listener);
    }

    private boolean handleMessage(final GameMessage message) {
        if (message instanceof UserListUpdatedMessage userListUpdatedMessage) {
            this.updateUserList(userListUpdatedMessage.getUsers());
            return true;
        }
        return false;
    }

    private void updateUserList(final Set<User> newUsers) {
        final Set<User> updatedUsers = Set.copyOf(this.users.exchange(users -> {
            users.clear();
            newUsers.forEach(u -> users.put(u.peerId(), u));
            return users;
        }).values());
        this.userListChangedListeners.forEach(l -> l.accept(updatedUsers));
    }
}
