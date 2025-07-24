package org.albard.dubito.userManagement.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.albard.dubito.network.PeerId;
import org.albard.dubito.userManagement.User;

public final class UserService {
    private final Map<PeerId, User> users = new ConcurrentHashMap<>();
    private final Set<Consumer<User>> addedListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<User>> updatedListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<Consumer<User>> removedListeners = Collections.synchronizedSet(new HashSet<>());

    public int getUserCount() {
        return this.users.size();
    }

    public Set<User> getUsers() {
        return Set.copyOf(this.users.values());
    }

    public void addUser(final User user) {
        if (this.users.putIfAbsent(user.peerId(), user) == null) {
            this.addedListeners.forEach(l -> l.accept(user));
        }
    }

    public void removeUser(final PeerId id) {
        final User info = this.users.remove(id);
        if (info != null) {
            this.removedListeners.forEach(l -> l.accept(info));
        }
    }

    public User updatePeer(final PeerId id, Function<User, User> updater) {
        final User newUser = this.users.computeIfPresent(id, (k, i) -> updater.apply(i));
        if (newUser != null) {
            this.updatedListeners.forEach(l -> l.accept(newUser));
        }
        return newUser;
    }

    public void addPeerAddedListener(final Consumer<User> listener) {
        this.addedListeners.add(listener);
    }

    public void removePeerAddedListener(final Consumer<User> listener) {
        this.addedListeners.remove(listener);
    }

    public void addPeerUpdatedListener(final Consumer<User> listener) {
        this.updatedListeners.add(listener);
    }

    public void removePeerUpdatedListener(final Consumer<User> listener) {
        this.updatedListeners.remove(listener);
    }

    public void addPeerRemovedListener(final Consumer<User> listener) {
        this.removedListeners.add(listener);
    }

    public void removePeerRemovedListener(final Consumer<User> listener) {
        this.removedListeners.remove(listener);
    }
}
