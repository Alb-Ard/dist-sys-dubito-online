package org.albard.dubito.app.messaging.messages;

import java.util.List;
import java.util.Set;

import org.albard.dubito.app.network.PeerId;

public abstract class ErrorGameMessageBase extends GameMessageBase {
    private final List<String> errors;

    public ErrorGameMessageBase(final PeerId sender, final Set<PeerId> receipients, final List<String> errors) {
        super(sender, receipients);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return List.copyOf(this.errors);
    }
}
