package org.albard.dubito.app;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public final class UserEndPoint {
    private final String host;
    private final int port;

    private UserEndPoint(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public static UserEndPoint createFromValues(String host, int port) {
        try {
            final InetAddress hostAddress = InetAddress.getByName(host);
            if (host == null || host.length() <= 0 || hostAddress == null || port <= 0) {
                return null;
            }
            return new UserEndPoint(host, port);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static UserEndPoint createFromAddress(SocketAddress address) {
        if (address instanceof InetSocketAddress socketAddress) {
            return new UserEndPoint(socketAddress.getHostString(), socketAddress.getPort());
        }
        return null;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public SocketAddress toSocketAddress() {
        return new InetSocketAddress(this.getHost(), this.getPort());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserEndPoint other = (UserEndPoint) obj;
        if (host == null && other.host != null) {
            return false;
        }
        return host.equals(other.host) && port == other.port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
