package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.internal.Instruction.InstructionType;
import se.omfilm.gameboy.util.DebugPrinter;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Debugger {
    private final Deque<EmulatorState> stateStack = new LinkedList<>();
    private final List<Breakpoint> breakpoints = new ArrayList<>();

    private final ReentrantLock pauseLock = new ReentrantLock();
    private final Condition pausedCondition = pauseLock.newCondition();

    public void update(EmulatorState state) {
        stateStack.add(state);
        while (stateStack.size() > 32) {
            stateStack.remove();
        }

        try {
            pauseLock.lock();
            if (breakpoints.stream().anyMatch(bp -> bp.matches(state))) {
                pausedCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
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
        EmulatorState state = getCurrentState();
        if (breakpoint.matches(state) && breakpoints.stream().noneMatch(bp -> bp.matches(state))) {
            step();
        }
    }

    private class AnyBreakpoint implements Breakpoint {
        public boolean matches(EmulatorState state) {
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

        public boolean matches(EmulatorState state) {
            return state.programCounter().read() == programCounter;
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

        public boolean matches(EmulatorState state) {
            return state.instructionType() == instruction;
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

        public boolean matches(EmulatorState state) {
            return state.memory().isWritten(address);
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

        public boolean matches(EmulatorState state) {
            return state.memory().isRead(address);
        }

        public String displayText() {
            return "Read: " + DebugPrinter.hex(address, 4);
        }
    }
}
