package org.albard.dubito.app;

public interface ObservableCloseable {
    @FunctionalInterface
    public interface ClosedListener {
        void closed();
    }

    void addClosedListener(ClosedListener listener);

    void removeClosedListener(ClosedListener listener);
}
