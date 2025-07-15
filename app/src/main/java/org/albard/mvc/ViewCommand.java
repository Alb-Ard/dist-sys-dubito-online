package org.albard.mvc;

public interface ViewCommand<X> {
    void addListener(final X listener);

    void removeListener(final X listener);
}