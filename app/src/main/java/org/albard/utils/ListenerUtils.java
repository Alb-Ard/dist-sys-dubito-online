package org.albard.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ListenerUtils {
    private ListenerUtils() {
    }

    public static void runAll(final Collection<Runnable> listeners) {
        List.copyOf(listeners).forEach(l -> l.run());
    }

    public static <X> void runAll(final Collection<Consumer<X>> listeners, final X value) {
        List.copyOf(listeners).forEach(l -> l.accept(value));
    }

    public static <X, Y> void runAll(final Collection<BiConsumer<X, Y>> listeners, final X a, final Y b) {
        List.copyOf(listeners).forEach(l -> l.accept(a, b));
    }
}
