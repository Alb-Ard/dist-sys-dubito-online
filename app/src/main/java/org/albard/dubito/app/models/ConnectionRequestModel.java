package org.albard.dubito.app.models;

import org.albard.mvc.AbstractModel;
import org.albard.mvc.ModelProperty;

public final class ConnectionRequestModel extends AbstractModel<ConnectionRequestModel> {
    public static final ModelProperty<String> ADDRESS_PROPERTY = defineProperty("address", String.class);
    public static final ModelProperty<Integer> PORT_PROPERTY = defineProperty("port", Integer.class);

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
        this.firePropertyChange(ADDRESS_PROPERTY, this.address, () -> this.address = address);
    }

    public void setPort(final int port) {
        this.firePropertyChange(PORT_PROPERTY, this.port, () -> this.port = port);
    }
}