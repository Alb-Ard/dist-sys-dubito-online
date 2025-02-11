package org.albard.dubito.app.network;

import java.net.Socket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PeerEndPointPair {
    private final PeerEndPoint localEndPoint;
    private final PeerEndPoint remoteEndPoint;

    @JsonCreator
    public PeerEndPointPair(@JsonProperty("local") final PeerEndPoint localEndPoint,
            @JsonProperty("remote") final PeerEndPoint remoteEndPoint) {
        this.localEndPoint = localEndPoint;
        this.remoteEndPoint = remoteEndPoint;
    }

    public static PeerEndPointPair createFromSocket(final Socket socket) {
        return new PeerEndPointPair(PeerEndPoint.createFromAddress(socket.getLocalSocketAddress()),
                PeerEndPoint.createFromAddress(socket.getRemoteSocketAddress()));
    }

    public PeerEndPoint getLocalEndPoint() {
        return this.localEndPoint;
    }

    public PeerEndPoint getRemoteEndPoint() {
        return this.remoteEndPoint;
    }

    @JsonIgnore
    public PeerEndPointPair getReversed() {
        return new PeerEndPointPair(this.getRemoteEndPoint(), this.getLocalEndPoint());
    }

    @Override
    public String toString() {
        return this.getLocalEndPoint() + " -> " + this.getRemoteEndPoint();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localEndPoint == null) ? 0 : localEndPoint.hashCode());
        result = prime * result + ((remoteEndPoint == null) ? 0 : remoteEndPoint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PeerEndPointPair other = (PeerEndPointPair) obj;
        if (localEndPoint == null) {
            if (other.localEndPoint != null)
                return false;
        } else if (!localEndPoint.equals(other.localEndPoint))
            return false;
        if (remoteEndPoint == null) {
            if (other.remoteEndPoint != null)
                return false;
        } else if (!remoteEndPoint.equals(other.remoteEndPoint))
            return false;
        return true;
    }
}
