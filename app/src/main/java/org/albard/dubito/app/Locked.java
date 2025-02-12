package org.albard.dubito.app;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Locked<T> {
    private final Lock lock = new ReentrantLock();
    private T value;

    private Locked(final T value) {
        this.value = value;
    }

    public static <T> Locked<T> of(final T value) {
        return new Locked<T>(value);
    }

    public boolean compareAndSet(Predicate<T> condition, Function<T, T> setter) {
        try {
            lock.lock();
            if (condition.test(this.value)) {
                this.value = setter.apply(this.value);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public T exchange(Function<T, T> mapper) {
        try {
            lock.lock();
            final T oldValue = value;
            this.value = mapper.apply(this.value);
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    public T getValue() {
        return this.value;
    }
}
