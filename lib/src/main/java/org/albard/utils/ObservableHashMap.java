package org.albard.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A map that provides observability when adding/removing items.
 * 
 * @implNote This implementation is thread-safe and supports concurrency.
 */
public final class ObservableHashMap<TKey, TValue> implements ObservableMap<TKey, TValue> {
    private final Map<TKey, TValue> map = Collections.synchronizedMap(new HashMap<>());
    private final Set<ObservableMapListener<TKey, TValue>> listeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void addListener(final ObservableMapListener<TKey, TValue> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(ObservableMapListener<TKey, TValue> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public TValue put(final TKey key, final TValue value) {
        final TValue oldValue = this.map.put(key, value);
        this.listeners.forEach(l -> l.entryAdded(key, value));
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TValue remove(final Object key) {
        if (!this.containsKey(key)) {
            return null;
        }
        final TValue value = this.map.remove(key);
        this.listeners.forEach(l -> l.entryRemoved((TKey) key, value));
        return value;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.map.containsKey(value);
    }

    @Override
    public TValue get(final Object key) {
        return this.map.get(key);
    }

    @Override
    public void putAll(Map<? extends TKey, ? extends TValue> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<TKey> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<TValue> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<TKey, TValue>> entrySet() {
        return this.map.entrySet();
    }
}
