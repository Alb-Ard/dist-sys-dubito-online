package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.UserEndPoint;

public abstract class GameMessageBase implements GameMessage {
    private final UserEndPoint sender;
    private final Set<UserEndPoint> receipients;

    public GameMessageBase(UserEndPoint sender, Set<UserEndPoint> receipients) {
        this.sender = sender;
        this.receipients = Set.copyOf(receipients);
    }

    public UserEndPoint getSender() {
        return this.sender;
    }

    public Set<UserEndPoint> getReceipients() {
        return Set.copyOf(this.receipients);
    }
}
