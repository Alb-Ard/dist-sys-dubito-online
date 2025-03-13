package org.albard.dubito.userManagement.messages;

import java.util.Set;

import org.albard.dubito.messaging.messages.GameMessageBase;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.userManagement.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UserListUpdatedMessage extends GameMessageBase {
    private final Set<User> users;

    @JsonCreator
    public UserListUpdatedMessage(@JsonProperty("sender") final PeerId sender,
            @JsonProperty("receipients") final Set<PeerId> receipients, @JsonProperty("users") final Set<User> users) {
        super(sender, receipients);
        this.users = Set.copyOf(users);
    }

    public Set<User> getUsers() {
        return this.users;
    }
}
