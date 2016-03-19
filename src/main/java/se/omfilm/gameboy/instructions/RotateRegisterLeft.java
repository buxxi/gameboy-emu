package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class RotateRegisterLeft implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;
    private final CarryTransfer carryTransfer;

    public RotateRegisterLeft(RegisterReader source, RegisterWriter target, CarryTransfer carryTransfer) {
        this.source = source;
        this.target = target;
        this.carryTransfer = carryTransfer;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int addOldCarry = carryTransfer.resolveCarry(flags, n);

        int result = ((n << 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b1000_0000) != 0;

        target.write(registers, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    private static int carryFromFlags(Flags flags, int n) {
        return flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
    }

    private static int carryFromBit7(Flags flags, int n) {
        return (n & 0b1000_0000) >> 7;
    }

    public static Instruction A() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA, RotateRegisterLeft::carryFromBit7);
    }

    public static Instruction AthroughCarry() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA, RotateRegisterLeft::carryFromFlags);
    }

    public static Instruction CthroughCarry() {
        return new RotateRegisterLeft(Registers::readC, Registers::writeC, RotateRegisterLeft::carryFromFlags);
    }

    private interface CarryTransfer {
        int resolveCarry(Flags flags, int n);
    }
}
