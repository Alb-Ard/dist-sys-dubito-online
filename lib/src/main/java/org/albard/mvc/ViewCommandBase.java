package org.albard.mvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ViewCommandBase<X> implements ViewCommand<X> {
    private final Set<X> listeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void addListener(final X listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(final X listener) {
        this.listeners.remove(listener);
    }

    protected Set<X> getListeners() {
        return Set.copyOf(this.listeners);
    }
}