package org.albard.dubito.app.messaging.messages;

import java.util.Set;

import org.albard.dubito.app.UserEndPoint;

public interface GameMessage {
    UserEndPoint getSender();

    Set<UserEndPoint> getReceipients();
}
