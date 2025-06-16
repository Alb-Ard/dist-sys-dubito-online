package org.albard.utils;

import java.util.Map;

public interface ObservableMap<TKey, TValue> extends Map<TKey, TValue> {
    public static <TKey, TValue> ObservableMap<TKey, TValue> createEmpty() {
        return new ObservableHashMap<TKey, TValue>();
    }

    public void addListener(ObservableMapListener<TKey, TValue> listener);

    public void removeListener(ObservableMapListener<TKey, TValue> listener);
}
