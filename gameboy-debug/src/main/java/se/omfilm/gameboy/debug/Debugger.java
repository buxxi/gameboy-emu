package se.omfilm.gameboy.debug;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Debugger {
    private final Deque<EmulatorState> stateStack = new LinkedList<>();

    private boolean paused = false;
    private final ReentrantLock pauseLock = new ReentrantLock();
    private final Condition pausedCondition = pauseLock.newCondition();

    public void update(EmulatorState state) {
        stateStack.add(state);
        while (stateStack.size() > 32) {
            stateStack.remove();
        }

        if (paused) {
            try {
                pauseLock.lock();
                pausedCondition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                pauseLock.unlock();
            }
        }
    }

    public EmulatorState getCurrentState() {
        return stateStack.peekLast();
    }

    public void pause() {
        paused = !paused;
        if (!paused) {
            try {
                pauseLock.lock();
                pausedCondition.signalAll();
            } finally {
                pauseLock.unlock();
            }
        }
    }

    public void step() {
        if (paused) {
            try {
                pauseLock.lock();
                pausedCondition.signalAll();
            } finally {
                pauseLock.unlock();
            }
        }
    }
}
