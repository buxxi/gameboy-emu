package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.Instruction.InstructionType;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class EmulatorState {
    private InstructionType instructionType;
    private ProgramCounter programCounter;
    private StackPointer stackPointer;
    private Flags flags;
    private Interrupts interrupts;
    private Registers registers;

    public EmulatorState(CPU cpu, Memory memory) {
        instructionType = readInstructionType(cpu, memory);
        programCounter = new RecordedProgramCounter(cpu.programCounter());
        stackPointer = new RecordedStackPointer(cpu.stackPointer());
        flags = new RecordedFlags(cpu.flags());
        interrupts = new RecordedInterrupts(cpu.interrupts());
        registers = new RecordedRegisters(cpu.registers());
    }

    public InstructionType instructionType() {
        return instructionType;
    }

    public ProgramCounter programCounter() {
        return programCounter;
    }

    public StackPointer stackPointer() {
        return stackPointer;
    }

    public Flags flags() {
        return flags;
    }

    public Interrupts interrupts() {
        return interrupts;
    }

    public Registers registers() {
        return registers;
    }

    private InstructionType readInstructionType(CPU cpu, Memory memory) {
        InstructionType type = InstructionType.fromOpCode(memory.readByte(cpu.programCounter().read()));
        if (type == InstructionType.CB) {
            type = InstructionType.fromOpCode(type.opcode(), memory.readByte(cpu.programCounter().read() + 1));
        }
        return type;
    }

    private <T extends Enum<T>> Map<T, Boolean> toMap(Class<T> type, T[] values, Function<T, Boolean> func) {
        Map<T, Boolean> result = new EnumMap<>(type);
        for (T value : values) {
            result.put(value, func.apply(value));
        }
        return result;
    }

    private class RecordedProgramCounter implements ProgramCounter {
        private final int value;

        public RecordedProgramCounter(ProgramCounter delegate) {
            value = delegate.read();
        }

        public int read() {
            return value;
        }

        public void write(int data) {
            throw new UnsupportedOperationException("write() not supported in " + getClass().getName());
        }
    }

    private class RecordedStackPointer implements StackPointer {
        private final int value;

        public RecordedStackPointer(StackPointer delegate) {
            value = delegate.read();
        }

        public void write(int value) {
            throw new UnsupportedOperationException("write() not supported in " + getClass().getName());
        }

        public int read() {
            return value;
        }
    }

    private class RecordedFlags implements Flags {
        private final Map<Flag, Boolean> data;
        private final boolean interruptsDisabled;

        public RecordedFlags(Flags delegate) {
            data = toMap(Flag.class, Flag.values(), delegate::isSet);
            interruptsDisabled = delegate.isInterruptsDisabled();
        }

        public boolean isSet(Flag flag) {
            return data.getOrDefault(flag, false);
        }

        public boolean isInterruptsDisabled() {
            return interruptsDisabled;
        }

        public void set(Flag flag, boolean set) {
            throw new UnsupportedOperationException("set() not supported in " + getClass().getName());
        }

        public void setInterruptsDisabled(boolean disabled) {
            throw new UnsupportedOperationException("setInterruptsDisabled() not supported in " + getClass().getName());
        }
    }

    private class RecordedInterrupts implements Interrupts {
        private final Map<Interrupt, Boolean> enabled;
        private final Map<Interrupt, Boolean> requested;

        public RecordedInterrupts(Interrupts delegate) {
            enabled = toMap(Interrupt.class, Interrupt.values(), delegate::enabled);
            requested = toMap(Interrupt.class, Interrupt.values(), delegate::enabled);
        }

        public void enable(Interrupt interrupt, boolean enabled) {
            throw new UnsupportedOperationException("enable() not supported in " + getClass().getName());
        }

        public void request(Interrupt interrupt, boolean requested) {
            throw new UnsupportedOperationException("request() not supported in " + getClass().getName());
        }

        public boolean enabled(Interrupt interrupt) {
            return enabled.getOrDefault(interrupt, false);
        }

        public boolean requested(Interrupt interrupt) {
            return requested.getOrDefault(interrupt, false);
        }
    }

    private class RecordedRegisters implements Registers {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;
        private final int f;
        private final int h;
        private final int l;

        public RecordedRegisters(Registers delegate) {
            this.a = delegate.readA();
            this.b = delegate.readB();
            this.c = delegate.readC();
            this.d = delegate.readD();
            this.e = delegate.readE();
            this.f = delegate.readF();
            this.h = delegate.readH();
            this.l = delegate.readL();
        }

        public int readA() {
            return a;
        }

        public int readB() {
            return b;
        }

        public int readC() {
            return c;
        }

        public int readD() {
            return d;
        }

        public int readE() {
            return e;
        }

        public int readF() {
            return f;
        }

        public int readH() {
            return h;
        }

        public int readL() {
            return l;
        }

        public void writeA(int val) {
            throw new UnsupportedOperationException("writeA() not supported in " + getClass().getName());
        }

        public void writeB(int val) {
            throw new UnsupportedOperationException("writeB() not supported in " + getClass().getName());
        }

        public void writeC(int val) {
            throw new UnsupportedOperationException("writeC() not supported in " + getClass().getName());
        }

        public void writeD(int val) {
            throw new UnsupportedOperationException("writeD() not supported in " + getClass().getName());
        }

        public void writeE(int val) {
            throw new UnsupportedOperationException("writeE() not supported in " + getClass().getName());
        }

        public void writeF(int val) {
            throw new UnsupportedOperationException("writeF() not supported in " + getClass().getName());
        }

        public void writeH(int val) {
            throw new UnsupportedOperationException("writeH() not supported in " + getClass().getName());
        }

        public void writeL(int val) {
            throw new UnsupportedOperationException("writeL() not supported in " + getClass().getName());
        }
    }
}
