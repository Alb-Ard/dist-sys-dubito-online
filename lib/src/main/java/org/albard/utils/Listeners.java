package org.albard.utils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Listeners {
    private Listeners() {
    }

    public static void run(final Runnable listener) {
        runAll(Set.of(listener));
    }

    public static <X> void run(final Consumer<X> listener, final X value) {
        runAll(Set.of(listener), value);
    }

    public static <X, Y> void run(final BiConsumer<X, Y> listener, final X a, final Y b) {
        runAll(Set.of(listener), a, b);
    }

    public static void runAll(final Collection<Runnable> listeners) {
        List.copyOf(listeners).stream().filter(x -> x != null).forEach(l -> l.run());
    }

    public static <X> void runAll(final Collection<Consumer<X>> listeners, final X value) {
        List.copyOf(listeners).stream().filter(x -> x != null).forEach(l -> l.accept(value));
    }

    public static <X, Y> void runAll(final Collection<BiConsumer<X, Y>> listeners, final X a, final Y b) {
        List.copyOf(listeners).stream().filter(x -> x != null).forEach(l -> l.accept(a, b));
    }
}
