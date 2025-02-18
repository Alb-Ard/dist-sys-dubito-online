package org.albard.dubito.network;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PeerId(@JsonProperty("id") String id) {
    // Fixed-length of a UUID/GUID
    public static int LENGTH = 36;

    public static PeerId createNew() {
        return new PeerId(UUID.randomUUID().toString());
    }

    @Override
    public final String toString() {
        return this.id();
    }
}
