package org.albard.dubito.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ObservableHashMap<TKey, TValue> implements ObservableMap<TKey, TValue> {
    private final Map<TKey, TValue> map = Collections.synchronizedMap(new HashMap<>());
    private final Set<ObservableMapListener<TKey, TValue>> listeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public boolean putIfAbsent(final TKey key, final TValue value) {
        if (this.map.putIfAbsent(key, value) != null) {
            return false;
        }
        this.listeners.forEach(l -> l.entryAdded(key, value));
        return true;
    }

    @Override
    public boolean remove(final TKey key) {
        final TValue value = this.map.remove(key);
        if (value == null) {
            return false;
        }
        this.listeners.forEach(l -> l.entryRemoved(key, value));
        return true;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public TValue get(final TKey key) {
        return this.map.get(key);
    }

    @Override
    public Set<TKey> keySet() {
        return this.map.keySet();
    }

    @Override
    public void addListener(final ObservableMapListener<TKey, TValue> listener) {
        this.listeners.add(listener);
    }
}
