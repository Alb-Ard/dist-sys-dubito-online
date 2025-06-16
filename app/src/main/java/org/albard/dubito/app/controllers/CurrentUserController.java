package org.albard.dubito.app.controllers;

import org.albard.dubito.app.models.AppStateModel;

public final class CurrentUserController {
    private final AppStateModel stateModel;

    public CurrentUserController(AppStateModel stateModel) {
        this.stateModel = stateModel;
    }

    public void setName(final String newName) {
        this.stateModel.getUserClient().ifPresent(x -> x.requestSetName(newName));
    }
}
