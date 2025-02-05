package org.albard.dubito.app;

public interface ObservableMapListener<TKey, TValue> {
    void entryAdded(TKey key, TValue value);

    void entryRemoved(TKey key, TValue value);
}
