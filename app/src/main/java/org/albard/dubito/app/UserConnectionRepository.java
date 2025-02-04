package org.albard.dubito.app;

import java.net.InetSocketAddress;

public interface UserConnectionRepository<T> {
    public static <T> UserConnectionRepository<T> createEmpty() {
        return new MapUserConnectionRepository<T>();
    }

    public boolean addUser(InetSocketAddress remoteEndPoint, T connection);

    public boolean removeUser(InetSocketAddress remoteEndPoint);

    public int getUserCount();

    public void clear();

    public T getUser(InetSocketAddress endPoint);

    public InetSocketAddress[] getAllUserEndPoints();
}
