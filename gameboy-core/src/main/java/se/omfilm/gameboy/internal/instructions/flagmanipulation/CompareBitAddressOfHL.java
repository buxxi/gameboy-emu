package se.omfilm.gameboy.internal.instructions.flagmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryReadInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class CompareBitAddressOfHL implements MemoryReadInstruction {
    private final int mask;

    private CompareBitAddressOfHL(int mask) {
        this.mask = mask;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        boolean isSet = (n & mask) != 0;

        flags.set(Flags.Flag.ZERO, !isSet);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, true);

        return totalCycles();
    }

    public int totalCycles() {
        return 12;
    }

    public static Instruction bit0() {
        return new CompareBitAddressOfHL(0b0000_0001);
    }

    public static Instruction bit1() {
        return new CompareBitAddressOfHL(0b0000_0010);
    }

    public static Instruction bit2() {
        return new CompareBitAddressOfHL(0b0000_0100);
    }

    public static Instruction bit3() {
        return new CompareBitAddressOfHL(0b0000_1000);
    }

    public static Instruction bit4() {
        return new CompareBitAddressOfHL(0b0001_0000);
    }

    public static Instruction bit5() {
        return new CompareBitAddressOfHL(0b0010_0000);
    }

    public static Instruction bit6() {
        return new CompareBitAddressOfHL(0b0100_0000);
    }

    public static Instruction bit7() {
        return new CompareBitAddressOfHL(0b1000_0000);
    }
}
