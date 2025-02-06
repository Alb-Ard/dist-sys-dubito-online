package org.albard.dubito.app.messaging;

import org.albard.dubito.app.network.PeerId;

public interface MessageDispatcher extends MessageSender, MessageReceiver {
    public void addPeer(final PeerId key, final MessageSender sender, final MessageReceiver receiver);

    public void removePeer(final PeerId key);

    public int getPeerCount();
}
