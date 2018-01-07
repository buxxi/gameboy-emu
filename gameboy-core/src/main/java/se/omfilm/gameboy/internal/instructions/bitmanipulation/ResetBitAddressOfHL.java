package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryModifyInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class ResetBitAddressOfHL implements MemoryModifyInstruction {
    private final int bit;

    private ResetBitAddressOfHL(int bit) {
        this.bit = bit;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        memory.writeByte(address, n & (~(1 << bit)));
        return 16;
    }

    public static Instruction bit0() {
        return new ResetBitAddressOfHL(0);
    }

    public static Instruction bit1() {
        return new ResetBitAddressOfHL(1);
    }

    public static Instruction bit2() {
        return new ResetBitAddressOfHL(2);
    }

    public static Instruction bit3() {
        return new ResetBitAddressOfHL(3);
    }

    public static Instruction bit4() {
        return new ResetBitAddressOfHL(4);
    }

    public static Instruction bit5() {
        return new ResetBitAddressOfHL(5);
    }

    public static Instruction bit6() {
        return new ResetBitAddressOfHL(6);
    }

    public static Instruction bit7() {
        return new ResetBitAddressOfHL(7);
    }
}
