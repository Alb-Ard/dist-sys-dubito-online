package org.albard.dubito.userManagement;

import org.albard.dubito.network.PeerId;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record User(@JsonProperty("id") PeerId peerId, @JsonProperty("name") String name) {
    public User changeName(final String newName) {
        return new User(this.peerId(), newName);
    }
}
