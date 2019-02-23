package se.omfilm.gameboy.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Runner {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Schedules a callable to be called at a fixed fps until it return false.
     *
     * This method blocks the current thread and no parallel calls should be made.
     */
    public static void atFrequency(Callable<Boolean> callable, int fps) throws Exception {
        long rate = SECONDS.toNanos(1) / fps;

        AtomicReference<Exception> exception = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        ScheduledFuture<?> await = executor.scheduleAtFixedRate(() -> {
            try {
                if (!callable.call()) {
                    latch.countDown();
                }
            } catch (Exception e) {
                exception.set(e);
                latch.countDown();
            }
        }, 0, rate, NANOSECONDS);

        latch.await();
        await.cancel(false);

        if (exception.get() != null) {
            throw new Exception(exception.get());
        }
    }

    /**
     * Runs the callable until it returns false as fast as possible
     */
    public static void atMaximumCapacity(Callable<Boolean> callable) throws Exception {
        boolean result;
        do {
            result = callable.call();
        } while (result);
    }

    /**
     * Runs the callable a fixed amount of times
     */
    public static void times(Callable<Integer> callable, int times) throws Exception {
        int counter = 0;
        while (counter < times) {
            counter += callable.call();
        }
    }

    /**
     * Returns a Counter class that calls the Runnable once per number of calls to the step-method.
     */
    public static Counter counter(Runnable runnable, int interval) {
        return new Counter(runnable, interval);
    }

    public static class Counter {
        private final Runnable runnable;
        private final int interval;
        private int counter;

        public Counter(Runnable runnable, int interval) {
            this.runnable = runnable;
            this.interval = interval;
            this.counter = interval;
        }

        public void step() {
            if (counter > 0) {
                counter--;
            } else {
                counter = interval;
                runnable.run();
            }
        }

        public boolean isFirstHalf() {
            return counter > (interval / 2);
        }
    }
}
