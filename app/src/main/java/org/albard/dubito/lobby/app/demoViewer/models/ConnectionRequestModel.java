package org.albard.dubito.lobby.app.demoViewer.models;

import com.jgoodies.binding.beans.Model;

public final class ConnectionRequestModel extends Model {
    public static final String ADDRESS_PROPERTY = "address";
    public static final String PORT_PROPERTY = "port";

    private String address;
    private int port;

    public ConnectionRequestModel(final String address, final int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public void setAddress(final String address) {
        final String oldAddress = this.address;
        this.address = address;
        this.firePropertyChange(ADDRESS_PROPERTY, oldAddress, address);
    }

    public void setPort(final int port) {
        final int oldPort = this.port;
        this.port = port;
        this.firePropertyChange(PORT_PROPERTY, oldPort, port);
    }
}