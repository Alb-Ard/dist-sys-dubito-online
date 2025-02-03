package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public final class UniqueUserConnectionRepository implements UserConnectionRepository {
    private final Set<InetSocketAddress> userSockets = new HashSet<>();

    @Override
    public boolean addUser(final InetSocketAddress remoteEndPoint) {
        return this.userSockets.add(remoteEndPoint);
    }

    @Override
    public boolean removeUser(final InetSocketAddress remoteEndPoint) {
        return this.userSockets.remove(remoteEndPoint);
    }
}
