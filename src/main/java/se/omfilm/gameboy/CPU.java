package se.omfilm.gameboy;

import java.util.EnumMap;
import java.util.Map;

public class CPU implements Registers {
    private Flags flags = new FlagsImpl();
    private ProgramCounter programCounter = new ProgramCounterImpl();
    private StackPointer stackPointer = new StackPointerImpl();

    private int a = 0;
    private int f = 0;
    private int b = 0;
    private int c = 0;
    private int h = 0;
    private int l = 0;
    private int d = 0;
    private int e = 0;

    private final Map<Instruction.InstructionType, Instruction> instructionMap;

    public CPU() {
        instructionMap = new EnumMap<>(Instruction.InstructionType.class);
        for (Instruction.InstructionType type : Instruction.InstructionType.values()) {
            instructionMap.put(type, type.instruction().get());
        }
    }

    public void runNext(MMU memory) {
        if (programCounter.read() == 0x100) {
            memory.bootSuccess();
            DebugPrinter.verifyBoot(this, this.stackPointer);
        }
        Instruction.InstructionType instructionType = Instruction.InstructionType.fromOpCode(memory.readByte(programCounter.increase()));
        if (instructionType == Instruction.InstructionType.CB) {
            instructionType = Instruction.InstructionType.fromOpCode(instructionType.opcode(), memory.readByte(programCounter.increase()));
        }

        Instruction instruction = instructionMap.getOrDefault(instructionType, unmappedInstruction(instructionType));
        System.out.println("Running instruction " + instructionType);
        instruction.execute(memory, this, this.flags, this.programCounter, this.stackPointer);
        DebugPrinter.debug(this.stackPointer, this.programCounter);
        DebugPrinter.debug(this);
    }

    private Instruction unmappedInstruction(Instruction.InstructionType instructionType) {
        return (memory, registers, flags, programCounter, stackPointer) -> {
            throw new UnsupportedOperationException(instructionType + " not implemented");
        };
    }

    public int readH() {
        return this.h;
    }

    public int readHL() {
        return this.h << 8 | this.l;
    }

    public void writeHL(int val) {
        verify(val, 0xFFFF);
        writeH(val >> 8);
        writeL(val & 0x00FF);
    }

    public int readL() {
        return this.l;
    }

    public void writeL(int val) {
        verify(val, 0xFF);
        this.l = val;
    }

    public void writeH(int val) {
        verify(val, 0xFF);
        this.h = val;
    }

    public int readDE() {
        return this.d << 8 | this.e;
    }

    public void writeDE(int val) {
        verify(val, 0xFFFF);
        writeD(val >> 8);
        writeE(val & 0x00FF);
    }

    public void writeE(int val) {
        verify(val, 0xFF);
        this.e = val;
    }

    public void writeD(int val) {
        verify(val, 0xFF);
        this.d = val;
    }

    public int readA() {
        return this.a;
    }

    public int readF() {
        return this.f;
    }

    public void writeF(int val) {
        this.f = val;
    }

    public int readAF() {
        return this.a << 8 | this.f;
    }

    public void writeA(int val) {
        verify(val, 0xFF);
        this.a = val;
    }

    public int readC() {
        return this.c;
    }

    public int readB() {
        return this.b;
    }

    public void writeC(int val) {
        verify(val, 0xFF);
        this.c = val;
    }

    public void writeB(int val) {
        verify(val, 0xFF);
        this.b = val;
    }

    public int readBC() {
        return this.b << 8 | this.c;
    }

    public void writeBC(int val) {
        verify(val, 0xFFFF);
        writeB(val >> 8);
        writeC(val & 0x00FF);
    }

    public int readD() {
        return this.d;
    }

    public int readE() {
        return this.e;
    }

    private void verify(int val, int maxValue) {
        if (val < 0 || val > maxValue) {
            throw new IllegalStateException("Can't write value " + DebugPrinter.hex(val, 4) + ", not in range " + DebugPrinter.hex(0, 4) + "-" + DebugPrinter.hex(maxValue, 4));
        }
    }

    private static class ProgramCounterImpl implements ProgramCounter {
        private int value = 0;

        public int increase() {
            return increase(1);
        }

        public int increase(int amount) {
            int oldValue = value;
            value = (value + amount) % 0xFFFF;
            return oldValue;
        }

        public int read() {
            return value;
        }

        public void write(int data) {
            this.value = data;
        }
    }

    private static class StackPointerImpl implements StackPointer {
        private int value = 0;

        public void write(int value) {
            this.value = value;
        }

        public int read() {
            return value;
        }
    }

    private class FlagsImpl implements Flags {
        public boolean isSet(Flag flag) {
            return (readF() & flag.mask) != 0;
        }

        public void set(Flag... flags) {
            int result = 0;
            for (Flag flag : flags) {
                result = result | flag.mask;
            }
            writeF(result);
        }
    }
}
