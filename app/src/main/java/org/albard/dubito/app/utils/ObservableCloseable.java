package org.albard.dubito.app.utils;

public interface ObservableCloseable {
    @FunctionalInterface
    public interface ClosedListener {
        void closed();
    }

    void addClosedListener(ClosedListener listener);

    void removeClosedListener(ClosedListener listener);
}
