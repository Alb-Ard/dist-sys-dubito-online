package org.albard.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Locked<T> {
    private final Lock lock;
    private T value;

    private Locked(final T value, final Lock lock) {
        this.value = value;
        this.lock = lock;
    }

    public static <T> Locked<T> of(final T value) {
        return new Locked<T>(value, new ReentrantLock());
    }

    public static <T> Locked<T> withExistingLock(final T value, final Lock lock) {
        return new Locked<T>(value, lock);
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
