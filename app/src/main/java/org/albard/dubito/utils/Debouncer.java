package org.albard.dubito.utils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Debouncer {
    private final Duration duration;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Locked<Long> counter = Locked.of(0L);

    public Debouncer(Duration duration) {
        this.duration = duration;
    }

    public void Debounce(final Runnable action) {
        long[] localCounter = new long[] { -1 };
        this.counter.exchange(counter -> {
            counter++;
            localCounter[0] = counter;
            return counter;
        });
        executor.execute(() -> {
            try {
                Thread.sleep(duration);
            } catch (final Exception ex) {

            } finally {
                this.counter.exchange(counter -> {
                    if (localCounter[0] == counter) {
                        action.run();
                    }
                    return counter;
                });
            }
        });
    }
}
