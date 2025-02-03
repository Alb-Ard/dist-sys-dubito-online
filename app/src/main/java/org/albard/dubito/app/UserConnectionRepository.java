package org.albard.dubito.app;

import java.net.InetSocketAddress;

public interface UserConnectionRepository {
    public static UserConnectionRepository create() {
        return new UniqueUserConnectionRepository();
    }

    public boolean addUser(InetSocketAddress remoteEndPoint);

    public boolean removeUser(InetSocketAddress remoteEndPoint);
}
