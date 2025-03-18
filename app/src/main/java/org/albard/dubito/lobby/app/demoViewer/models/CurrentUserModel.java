package org.albard.dubito.lobby.app.demoViewer.models;

import com.jgoodies.binding.beans.Model;

public final class CurrentUserModel extends Model {
    public static final String NAME_PROPERTY = "name";

    private String name;

    public CurrentUserModel(final String userName) {
        this.name = userName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        final String oldName = this.name;
        this.name = name;
        this.firePropertyChange(NAME_PROPERTY, oldName, name);
    }
}
