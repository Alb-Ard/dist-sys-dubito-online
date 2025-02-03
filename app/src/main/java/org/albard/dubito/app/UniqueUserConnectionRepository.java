package org.albard.dubito.app;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public final class UniqueUserConnectionRepository implements UserConnectionRepository {
    private final Set<Socket> userSockets = new HashSet<>();

    @Override
    public boolean addUser(final Socket userSocket) {
        return this.userSockets.add(userSocket);
    }
}
