package org.albard.dubito.app.models;

import java.util.Optional;

import org.albard.dubito.userManagement.client.UserClient;
import org.albard.mvc.AbstractModel;
import org.albard.mvc.ModelAdapter;
import org.albard.mvc.ModelProperty;
import org.albard.mvc.ModelPropertyChangeEvent;

public final class CurrentUserModel extends AbstractModel<CurrentUserModel> {
    public static final ModelProperty<String> NAME_PROPERTY = defineProperty("name");

    private String name = "";

    public CurrentUserModel(final AppStateModel stateModel) {
        new ModelAdapter<>(stateModel).addModelPropertyChangeListener(AppStateModel.USER_CLIENT_PROPERTY,
                this::onUserClientChanged);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.firePropertyChange(NAME_PROPERTY, this.name, () -> this.name = name);
    }

    private void onUserClientChanged(final ModelPropertyChangeEvent<Optional<UserClient>> e) {
        this.setName(e.getNewTypedValue().map(x -> x.getLocalUser().name()).orElse(""));
    }
}
