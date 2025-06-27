package org.albard.mvc;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ViewCommandBase<X> implements ViewCommand<X> {
    private final Set<WeakReference<X>> listeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void addListener(final X listener) {
        this.listeners.add(new WeakReference<>(listener));
    }

    @Override
    public void removeListener(final X listener) {
        this.listeners.remove(listener);
    }

    protected Set<X> getListeners() {
        return Set.copyOf(this.listeners).stream().map(x -> x.get()).filter(x -> x != null)
                .collect(Collectors.toUnmodifiableSet());
    }
}