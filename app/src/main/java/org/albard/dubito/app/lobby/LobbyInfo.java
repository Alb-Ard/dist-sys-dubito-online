package org.albard.dubito.app.lobby;

import java.util.ArrayList;
import java.util.List;

public final record LobbyInfo(String name, String password) {
    public List<Exception> validate() {
        final List<Exception> errors = new ArrayList<>();
        if (name == null) {
            errors.add(new Exception("name can't be null"));
        } else if (name.isBlank()) {
            errors.add(new Exception("name can't be blank"));
        }
        return errors;
    }
}
