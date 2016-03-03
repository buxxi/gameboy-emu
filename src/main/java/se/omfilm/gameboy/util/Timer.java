package se.omfilm.gameboy.util;

import java.util.concurrent.Callable;

public class Timer {
    public static void runForever(Callable<Void> callable, int fps) throws Exception {
        long expectedDiff = 1000 / fps;
        while (true) {
            long before = System.currentTimeMillis();
            callable.call();
            long after = System.currentTimeMillis();
            long diff = after - before;
            if (diff < expectedDiff) {
                long l = expectedDiff - diff;
                Thread.sleep(l);
            }
        }
    }

    public static void runTimes(Callable<Integer> callable, int times) throws Exception {
        int counter = 0;
        while (counter < times) {
            counter += callable.call();
        }
    }
}
