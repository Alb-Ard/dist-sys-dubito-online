package org.albard.dubito.app;

import java.net.Socket;

public interface UserConnectionRepository {
    public static UserConnectionRepository create() {
        return new UniqueUserConnectionRepository();
    }

    public boolean addUser(Socket userSocket);
}
