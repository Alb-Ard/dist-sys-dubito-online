package org.albard.dubito.app;

import java.net.InetSocketAddress;

public interface UserRepositoryListener<T> {
    void handleUserAdded(InetSocketAddress endPoint, T connection);

    void handleUserRemoved(InetSocketAddress endPoint, T connection);
}
