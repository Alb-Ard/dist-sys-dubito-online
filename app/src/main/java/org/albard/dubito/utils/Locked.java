package org.albard.dubito.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A wrapper that enables thread-safety when handling an instance of an object.
 */
public final class Locked<T> {
    private final Lock lock = new ReentrantLock();
    private T value;

    private Locked(final T value) {
        this.value = value;
    }

    /**
     * Constructs a new Locked object
     * 
     * @param <T>   The type of object
     * @param value The initial value
     * @return The Locked instance
     */
    public static <T> Locked<T> of(final T value) {
        return new Locked<T>(value);
    }

    /**
     * Does an atomic compare-and-set operation on the current value
     * 
     * @param condition The condition to satify for applying the setter
     * @param setter    The setter that updates the value
     * @return true if the condition returned true and the value was changed, false
     *         otherwise
     */
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

    /**
     * Updates the current value
     * 
     * @param mapper The mapper from the old value to the new value
     * @return The old value
     */
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

    /**
     * Gets the object inside this Locked instance. Note: Modifications on the
     * returned object are NOT thread safe.
     * 
     * @return The object instance
     */
    public T getValue() {
        return this.value;
    }
}
