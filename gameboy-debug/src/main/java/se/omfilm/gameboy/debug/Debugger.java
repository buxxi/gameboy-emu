package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.internal.Instruction.InstructionType;
import se.omfilm.gameboy.util.DebugPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Debugger {
    private final Deque<EmulatorState> stateStack = new LinkedList<>();
    private final List<Breakpoint> breakpoints = new ArrayList<>();
    private final List<Consumer<EmulatorState>> listeners = new ArrayList<>();

    private boolean paused = false;
    private final ReentrantLock pauseLock = new ReentrantLock();
    private final Condition pausedCondition = pauseLock.newCondition();

    public Debugger() {
        pause();
    }

    public void update(EmulatorState currentState) {
        stateStack.add(currentState);

        listeners.forEach(c -> c.accept(currentState));

        while (stateStack.size() > 32) {
            stateStack.remove();
        }
    }

    public void checkBreakpoints() {
        EmulatorState currentState = getCurrentState();
        try {
            pauseLock.lock();
            if (breakpoints.stream().anyMatch(bp -> bp.matches(currentState))) {
                paused = true;
                pausedCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            paused = false;
            pauseLock.unlock();
        }
    }

    public EmulatorState getCurrentState() {
        return stateStack.peekLast();
    }

    public void pause() {
        AnyBreakpoint any = new AnyBreakpoint();
        if (breakpoints.contains(any)) {
            breakpoints.remove(any);
            step();
        } else {
            breakpoints.add(any);
        }
    }

    public void step() {
        try {
            pauseLock.lock();
            pausedCondition.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void addBreakpoint(InstructionType instruction) {
        breakpoints.add(new InstructionBreakpoint(instruction));
    }

    public void addBreakpoint(int programCounter) {
        breakpoints.add(new ProgramCounterBreakpoint(programCounter));
    }

    public void addReadBreakpoint(int address) {
        breakpoints.add(new MemoryReadBreakpoint(address));
    }

    public void addWriteBreakpoint(int address) {
        breakpoints.add(new MemoryWriteBreakpoint(address));
    }

    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    public void removeBreakpoint(Breakpoint breakpoint) {
        breakpoints.remove(breakpoint);

        EmulatorState currentState = getCurrentState();
        if (breakpoint.matches(currentState) && breakpoints.stream().noneMatch(bp -> bp.matches(currentState))) {
            step();
        }
    }

    public Iterator<EmulatorState> getCallTrace() {
        return stateStack.descendingIterator();
    }

    public void writeLogFile(File logFile) throws FileNotFoundException {
        listeners.add(new LogFileWriter(logFile));
    }

    private class AnyBreakpoint implements Breakpoint {
        public boolean matches(EmulatorState currentState) {
            return true;
        }

        public String displayText() {
            return "*";
        }

        @Override
        public int hashCode() {
            return AnyBreakpoint.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(AnyBreakpoint.class);
        }
    }

    private class ProgramCounterBreakpoint implements Breakpoint {
        private final int programCounter;

        public ProgramCounterBreakpoint(int programCounter) {
            this.programCounter = programCounter;
        }

        public boolean matches(EmulatorState currentState) {
            return currentState.programCounter().read() == programCounter;
        }

        public String displayText() {
            return "PC: " + DebugPrinter.hex(programCounter, 4);
        }
    }

    private class InstructionBreakpoint implements Breakpoint {
        private final InstructionType instruction;

        public InstructionBreakpoint(InstructionType instruction) {
            this.instruction = instruction;
        }

        public boolean matches(EmulatorState currentState) {
            return currentState.instructionType() == instruction;
        }

        public String displayText() {
            return "Instr: " + instruction.name();
        }
    }

    private class MemoryWriteBreakpoint implements Breakpoint {
        private final int address;

        public MemoryWriteBreakpoint(int address) {
            this.address = address;
        }

        public boolean matches(EmulatorState currentState) {
            return currentState.memory().isWritten(address);
        }

        public String displayText() {
            return "Write: " + DebugPrinter.hex(address, 4);
        }
    }

    private class MemoryReadBreakpoint implements Breakpoint {
        private final int address;

        public MemoryReadBreakpoint(int address) {
            this.address = address;
        }

        public boolean matches(EmulatorState currentState) {
            return currentState.memory().isRead(address);
        }

        public String displayText() {
            return "Read: " + DebugPrinter.hex(address, 4);
        }
    }
}
