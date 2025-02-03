package org.albard.dubito.app;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SetUserConnectionRepository implements UserConnectionRepository {
    private final Set<InetSocketAddress> userSockets = Collections.synchronizedSet(new HashSet<>());

    @Override
    public boolean addUser(final InetSocketAddress remoteEndPoint) {
        return this.userSockets.add(remoteEndPoint);
    }

    @Override
    public boolean removeUser(final InetSocketAddress remoteEndPoint) {
        return this.userSockets.remove(remoteEndPoint);
    }

    @Override
    public int getUserCount() {
        return this.userSockets.size();
    }

    @Override
    public void clear() {
        this.userSockets.clear();
    }
}
