package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryModifyInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class RotateAddressOfHLLeft implements MemoryModifyInstruction {
    private final CarryTransfer carryTransfer;

    private RotateAddressOfHLLeft(CarryTransfer carryTransfer) {
        this.carryTransfer = carryTransfer;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        int addOldCarry = carryTransfer.resolveCarry(flags, n);

        int result = ((n << 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b1000_0000) != 0;

        memory.writeByte(address, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 16;
    }

    private static int carryFromFlags(Flags flags, int n) {
        return flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
    }

    private static int carryFromBit7(Flags flags, int n) {
        return (n & 0b1000_0000) >> 7;
    }

    public static Instruction bit7() {
        return new RotateAddressOfHLLeft(RotateAddressOfHLLeft::carryFromBit7);
    }

    public static Instruction flag() {
        return new RotateAddressOfHLLeft(RotateAddressOfHLLeft::carryFromFlags);
    }


    private interface CarryTransfer {
        int resolveCarry(Flags flags, int n);
    }
}
