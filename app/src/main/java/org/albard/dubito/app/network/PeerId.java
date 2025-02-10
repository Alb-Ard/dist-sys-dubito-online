package org.albard.dubito.app.network;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PeerId(@JsonProperty("id") String id) {
    private static Charset CHARSET = StandardCharsets.UTF_8;
    // Fixed-length of a UUID/GUID
    public static int LENGTH = 36;

    public static PeerId createNew() {
        return new PeerId(UUID.randomUUID().toString());
    }

    public static PeerId createFromBytes(final byte[] id) {
        return new PeerId(UUID.fromString(CHARSET.decode(ByteBuffer.wrap(id)).toString()).toString());
    }

    @JsonIgnore
    public byte[] getBytes() {
        return CHARSET.encode(id).array();
    }

    @Override
    public final String toString() {
        return this.id();
    }
}
