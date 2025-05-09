package org.albard.dubito.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PeerEndPoint {
    private final String host;
    private final int port;

    @JsonCreator
    private PeerEndPoint(@JsonProperty("host") final String host, @JsonProperty("port") final int port) {
        this.host = host;
        this.port = port;
    }

    public static PeerEndPoint createFromValues(final String host, final int port) {
        try {
            final InetAddress hostAddress = InetAddress.getByName(host);
            if (host == null || host.length() <= 0 || hostAddress == null || port < 0) {
                return null;
            }
            return new PeerEndPoint(host, port);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static PeerEndPoint createFromAddress(SocketAddress address) {
        if (address instanceof InetSocketAddress socketAddress) {
            final String[] hostParts = socketAddress.getHostString().split("/");
            return new PeerEndPoint(hostParts[hostParts.length - 1], socketAddress.getPort());
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
        PeerEndPoint other = (PeerEndPoint) obj;
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
