package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryModifyInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class ShiftAddressOfHLRight implements MemoryModifyInstruction {
    private final int oldMask;

    private ShiftAddressOfHLRight(int oldMask) {
        this.oldMask = oldMask;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        int result = ((n >> 1) | (n & oldMask)) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b0000_0001) != 0;

        memory.writeByte(address, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 16;
    }

    private static int keepBit7Mask() {
        return 0b1000_0000;
    }

    private static int resetBit7Mask() {
        return 0b0000_0000;
    }

    public static Instruction keepBit7() {
        return new ShiftAddressOfHLRight(keepBit7Mask());
    }

    public static Instruction resetBit7() {
        return new ShiftAddressOfHLRight(resetBit7Mask());
    }
}
