package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MapUserConnectionRepository<T> implements UserConnectionRepository<T> {
    private final Map<InetSocketAddress, T> connections = Collections.synchronizedMap(new HashMap<>());
    private final Set<UserRepositoryListener<T>> listeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public boolean addUser(final InetSocketAddress remoteEndPoint, final T connection) {
        if (this.connections.putIfAbsent(remoteEndPoint, connection) != null) {
            return false;
        }
        this.listeners.forEach(l -> l.handleUserAdded(remoteEndPoint, connection));
        return true;
    }

    @Override
    public boolean removeUser(final InetSocketAddress remoteEndPoint) {
        final T connection = this.connections.remove(remoteEndPoint);
        if (connection == null) {
            return false;
        }
        this.listeners.forEach(l -> l.handleUserRemoved(remoteEndPoint, connection));
        return true;
    }

    @Override
    public int getUserCount() {
        return this.connections.size();
    }

    @Override
    public void clear() {
        this.connections.clear();
    }

    @Override
    public T getUser(final InetSocketAddress endPoint) {
        return this.connections.get(endPoint);
    }

    @Override
    public InetSocketAddress[] getAllUserEndPoints() {
        return this.connections.keySet().toArray(new InetSocketAddress[this.connections.size()]);
    }

    @Override
    public void addUserListener(final UserRepositoryListener<T> userRepositoryListener) {
        this.listeners.add(userRepositoryListener);
    }
}
