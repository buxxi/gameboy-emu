package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.Instruction.InstructionType;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EmulatorState {
    private final InstructionType instructionType;
    private final ProgramCounter programCounter;
    private final StackPointer stackPointer;
    private final Flags flags;
    private final Interrupts interrupts;
    private final Registers registers;
    private final APUState apu;
    private final PPUState ppu;
    private final MemoryState memory;

    public EmulatorState(CPU cpu, Memory memory) {
        instructionType = readInstructionType(cpu, memory);
        programCounter = new RecordedProgramCounter(cpu.programCounter());
        stackPointer = new RecordedStackPointer(cpu.stackPointer());
        flags = new RecordedFlags(cpu.flags());
        interrupts = new RecordedInterrupts(cpu.interrupts());
        registers = new RecordedRegisters(cpu.registers());
        this.memory = new MemoryState();
        this.apu = new APUState(this.memory, memory);
        this.ppu = new PPUState(this.memory, memory);
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

    public APUState apu() {
        return apu;
    }

    public PPUState ppu() {
        return ppu;
    }

    public MemoryState memory() {
        return memory;
    }

    public Memory recordMemory(Memory memory) {
        return new RecordedMemory(this.memory, memory);
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

    public class MemoryState {
        private Map<Integer, Integer> reads = new HashMap<>();
        private Map<Integer, Integer> writes = new HashMap<>();

        public void updateRead(int address, int data) {
            reads.put(address, data);
        }

        public void updateWrite(int address, int data) {
            writes.put(address, data);
        }

        public boolean isWritten(int address) {
            return writes.containsKey(address);
        }

        public boolean isRead(int address) {
            return reads.containsKey(address);
        }
    }

    public class PPUState {
        private final IORegisterState lcdControl;
        private final IORegisterState lcdStatus;
        private final IORegisterState scrollY;
        private final IORegisterState scrollX;
        private final IORegisterState scanline;
        private final IORegisterState scanlineCompare;
        private final IORegisterState backgroundPalette;
        private final IORegisterState objectPalette0;
        private final IORegisterState objectPalette1;
        private final IORegisterState windowY;
        private final IORegisterState windowX;

        public PPUState(MemoryState state, Memory memory) {
            lcdControl = new IORegisterState(0xFF40, state, memory);
            lcdStatus = new IORegisterState(0xFF41, state, memory);
            scrollY = new IORegisterState(0xFF42, state, memory);
            scrollX = new IORegisterState(0xFF43, state, memory);
            scanline = new IORegisterState(0xFF44, state, memory);
            scanlineCompare = new IORegisterState(0xFF45, state, memory);
            backgroundPalette = new IORegisterState(0xFF47, state, memory);
            objectPalette0 = new IORegisterState(0xFF48, state, memory);
            objectPalette1 = new IORegisterState(0xFF49, state, memory);
            windowY = new IORegisterState(0xFF4A, state, memory);
            windowX = new IORegisterState(0xFF4B, state, memory);
        }

        public IORegisterState lcdControl() {
            return lcdControl;
        }

        public IORegisterState lcdStatus() {
            return lcdStatus;
        }

        public IORegisterState scrollY() {
            return scrollY;
        }

        public IORegisterState scrollX() {
            return scrollX;
        }

        public IORegisterState scanline() {
            return scanline;
        }

        public IORegisterState scanlineCompare() {
            return scanlineCompare;
        }

        public IORegisterState backgroundPalette() {
            return backgroundPalette;
        }

        public IORegisterState objectPalette0() {
            return objectPalette0;
        }

        public IORegisterState objectPalette1() {
            return objectPalette1;
        }

        public IORegisterState windowY() {
            return windowY;
        }

        public IORegisterState windowX() {
            return windowX;
        }
    }

    public class APUState {
        private final IORegisterState enabled;
        private final IORegisterState outputTerminal;
        private final IORegisterState channelControl;
        private final int[] waveRAM;
        private final SoundState sound1;
        private final SoundState sound2;
        private final SoundState sound3;
        private final SoundState sound4;

        public APUState(MemoryState state, Memory memory) {
            enabled = new IORegisterState(0xFF26, state, memory);
            outputTerminal = new IORegisterState(0xFF25, state, memory);
            channelControl = new IORegisterState(0xFF24, state, memory);
            waveRAM = new int[16];
            for (int i = 0; i < waveRAM.length; i++) {
                waveRAM[i] = memory.readByte(0xFF30 + i);
            }
            sound1 = new SoundState(
                    new IORegisterState(0xFF11, state, memory),
                    new IORegisterState(0xFF12, state, memory),
                    new IORegisterState(0xFF13, state, memory),
                    new IORegisterState(0xFF14, state, memory),
                    new IORegisterState(0xFF10, state, memory),
                    null,
                    null,
                    null,
                    null
            );
            sound2 = new SoundState(
                    new IORegisterState(0xFF16, state, memory),
                    new IORegisterState(0xFF17, state, memory),
                    new IORegisterState(0xFF18, state, memory),
                    new IORegisterState(0xFF19, state, memory),
                    null,
                    null,
                    null,
                    null,
                    null
            );
            sound3 = new SoundState(
                    new IORegisterState(0xFF1B, state, memory),
                    null,
                    new IORegisterState(0xFF1D, state, memory),
                    new IORegisterState(0xFF1E, state, memory),
                    null,
                    new IORegisterState(0xFF1A, state, memory),
                    new IORegisterState(0xFF1C, state, memory),
                    null,
                    null
            );
            sound4 = new SoundState(
                    new IORegisterState(0xFF20, state, memory),
                    new IORegisterState(0xFF21, state, memory),
                    null,
                    null,
                    null,
                    null,
                    null,
                    new IORegisterState(0xFF22, state, memory),
                    new IORegisterState(0xFF23, state, memory)
            );
        }

        public IORegisterState enabled() {
            return enabled;
        }

        public IORegisterState channelControl() {
            return channelControl;
        }

        public IORegisterState outputTerminal() {
            return outputTerminal;
        }

        public int[] waveRAM() {
            return waveRAM;
        }

        public SoundState[] soundStates() {
            return new SoundState[] { sound1, sound2, sound3, sound4 };
        }
    }

    public class SoundState {
        private final IORegisterState length;
        private final IORegisterState envelope;
        private final IORegisterState lowFrequency;
        private final IORegisterState highFrequence;
        private final IORegisterState sweep;
        private final IORegisterState onOff;
        private final IORegisterState outputLevel;
        private final IORegisterState polynomial;
        private final IORegisterState initial;

        public SoundState(IORegisterState length, IORegisterState envelope, IORegisterState lowFrequency, IORegisterState highFrequence, IORegisterState sweep, IORegisterState onOff, IORegisterState outputLevel, IORegisterState polynomial, IORegisterState initial) {
            this.length = length;
            this.envelope = envelope;
            this.lowFrequency = lowFrequency;
            this.highFrequence = highFrequence;
            this.sweep = sweep;
            this.onOff = onOff;
            this.outputLevel = outputLevel;
            this.polynomial = polynomial;
            this.initial = initial;
        }

        public IORegisterState length() {
            return length;
        }

        public IORegisterState envelope() {
            return envelope;
        }

        public IORegisterState lowFrequency() {
            return lowFrequency;
        }

        public IORegisterState highFrequency() {
            return highFrequence;
        }

        public IORegisterState sweep() {
            return sweep;
        }

        public IORegisterState onOff() {
            return onOff;
        }

        public IORegisterState outputLevel() {
            return outputLevel;
        }

        public IORegisterState polynomial() {
            return polynomial;
        }

        public IORegisterState initial() {
            return initial;
        }
    }

    public class IORegisterState {
        private final int address;
        private final MemoryState state;
        private final Memory memory;

        public IORegisterState(int address, MemoryState state, Memory memory) {
            this.address = address;
            this.state = state;
            this.memory = memory;
        }

        public int written() {
            return state.writes.getOrDefault(address, 0);
        }

        public int read() {
            return memory.readByte(address);
        }
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

    private class RecordedMemory implements Memory {
        private final MemoryState state;
        private final Memory delegate;

        public RecordedMemory(MemoryState state, Memory delegate) {
            this.state = state;
            this.delegate = delegate;
        }

        public int readByte(int address) {
            int result = delegate.readByte(address);
            state.updateRead(address, result);
            return result;
        }

        public void writeByte(int address, int data) {
            state.updateWrite(address, data);
            delegate.writeByte(address, data);
        }
    }
}
