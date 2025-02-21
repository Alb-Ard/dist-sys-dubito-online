package org.albard.dubito.utils;

public interface ObservableCloseable {
    @FunctionalInterface
    public interface ClosedListener {
        void closed();
    }

    void addClosedListener(ClosedListener listener);

    void removeClosedListener(ClosedListener listener);
}
