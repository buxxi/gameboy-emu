package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class RotateAddressOfHLRight implements Instruction {
    private final CarryTransfer carryTransfer;

    private RotateAddressOfHLRight(CarryTransfer carryTransfer) {
        this.carryTransfer = carryTransfer;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readWord(address);
        int addOldCarry = carryTransfer.resolveCarry(flags, n);

        int result = ((n >> 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b0000_0001) != 0;

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

    private static int carryFromBit0(Flags flags, int n) {
        return (n & 0b0000_0001);
    }

    public static Instruction bit0() {
        return new RotateAddressOfHLRight(RotateAddressOfHLRight::carryFromBit0);
    }

    public static Instruction flag() {
        return new RotateAddressOfHLRight(RotateAddressOfHLRight::carryFromFlags);
    }

    private interface CarryTransfer {
        int resolveCarry(Flags flags, int n);
    }
}
