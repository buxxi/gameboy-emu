package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class SetBitAddressOfHL implements Instruction {
    private final int bit;

    private SetBitAddressOfHL(int bit) {
        this.bit = bit;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        memory.writeByte(address, n | (1 << bit));
        return 16;
    }

    public static Instruction bit0() {
        return new SetBitAddressOfHL(0);
    }

    public static Instruction bit1() {
        return new SetBitAddressOfHL(1);
    }

    public static Instruction bit2() {
        return new SetBitAddressOfHL(2);
    }

    public static Instruction bit3() {
        return new SetBitAddressOfHL(3);
    }

    public static Instruction bit4() {
        return new SetBitAddressOfHL(4);
    }

    public static Instruction bit5() {
        return new SetBitAddressOfHL(5);
    }

    public static Instruction bit6() {
        return new SetBitAddressOfHL(6);
    }

    public static Instruction bit7() {
        return new SetBitAddressOfHL(7);
    }
}
