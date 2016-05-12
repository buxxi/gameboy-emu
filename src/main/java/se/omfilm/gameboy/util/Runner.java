package se.omfilm.gameboy.util;

import java.util.concurrent.Callable;

public class Runner {
    public static void atFrequency(Callable<Boolean> callable, int fps) throws Exception {
        long expectedDiff = 1000 / fps;
        boolean running = true;
        while (running) {
            long before = System.currentTimeMillis();
            running = callable.call();
            long after = System.currentTimeMillis();
            long diff = after - before;
            if (diff < expectedDiff) {
                long l = expectedDiff - diff;
                Thread.sleep(l);
            }
        }
    }

    public static void times(Callable<Integer> callable, int times) throws Exception {
        int counter = 0;
        while (counter < times) {
            counter += callable.call();
        }
    }
}
