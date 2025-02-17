package org.albard.dubito.app.lobby.app.demoViewer.models;

public final class ConnectionRequestModel extends AbstractModel {
    public static final String ADDRESS_PROPERTY_NAME = "address";
    public static final String PORT_PROPERTY_NAME = "port";

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
        this.firePropertyChange(ADDRESS_PROPERTY_NAME, this.address, address, x -> this.address = x);
    }

    public void setPort(final int port) {
        this.firePropertyChange(ADDRESS_PROPERTY_NAME, this.port, port, x -> this.port = x);
    }
}