package org.albard.dubito.app.messaging.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.albard.dubito.app.UserEndPoint;

public final class MessageHandlerChain implements MessageHandler {
    private final List<MessageHandler> handlers;

    public MessageHandlerChain(final Collection<MessageHandler> handlers) {
        this.handlers = new ArrayList<>(handlers);
    }

    @Override
    public boolean handleMessage(final UserEndPoint fromEndPoint, final Object message) {
        for (final MessageHandler handler : this.handlers) {
            try {
                if (handler.handleMessage(fromEndPoint, message)) {
                    return true;
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
