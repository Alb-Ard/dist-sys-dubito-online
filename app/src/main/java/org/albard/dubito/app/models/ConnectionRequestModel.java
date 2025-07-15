package org.albard.dubito.app.models;

import org.albard.mvc.AbstractModel;
import org.albard.mvc.ModelProperty;

public final class ConnectionRequestModel extends AbstractModel<ConnectionRequestModel> {
    public static final ModelProperty<String> ADDRESS_PROPERTY = defineProperty("address");
    public static final ModelProperty<String> PORT_PROPERTY = defineProperty("port");

    private String address;
    private String port;

    public ConnectionRequestModel(final String address, final String port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPort() {
        return this.port;
    }

    public void setAddress(final String address) {
        this.firePropertyChange(ADDRESS_PROPERTY, this.address, () -> this.address = address);
    }

    public void setPort(final String port) {
        this.firePropertyChange(PORT_PROPERTY, this.port, () -> this.port = port);
    }
}