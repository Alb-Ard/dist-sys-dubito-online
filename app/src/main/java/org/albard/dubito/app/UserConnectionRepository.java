package org.albard.dubito.app;

import java.net.InetSocketAddress;

public interface UserConnectionRepository {
    public static UserConnectionRepository create() {
        return new SetUserConnectionRepository();
    }

    public boolean addUser(InetSocketAddress remoteEndPoint);

    public boolean removeUser(InetSocketAddress remoteEndPoint);

    public int getUserCount();

    public void clear();
}
