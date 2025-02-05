package org.albard.dubito.app;

import java.util.Set;

public interface ObservableMap<TKey, TValue> {
    public static <TKey, TValue> ObservableMap<TKey, TValue> createEmpty() {
        return new ObservableHashMap<TKey, TValue>();
    }

    public boolean putIfAbsent(TKey key, TValue value);

    public boolean remove(TKey key);

    public int size();

    public void clear();

    public TValue get(TKey key);

    public Set<TKey> keySet();

    public void addListener(ObservableMapListener<TKey, TValue> listener);
}
