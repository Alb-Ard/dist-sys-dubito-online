package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MapUserConnectionRepository<T> implements UserConnectionRepository<T> {
    private final Map<InetSocketAddress, T> connections = Collections.synchronizedMap(new HashMap<>());

    @Override
    public boolean addUser(final InetSocketAddress remoteEndPoint, final T connection) {
        return this.connections.putIfAbsent(remoteEndPoint, connection) == null;
    }

    @Override
    public boolean removeUser(final InetSocketAddress remoteEndPoint) {
        return this.connections.remove(remoteEndPoint) != null;
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
}
