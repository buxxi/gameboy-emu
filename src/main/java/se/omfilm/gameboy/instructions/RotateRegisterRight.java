package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class RotateRegisterRight implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;
    private final CarryTransfer carryTransfer;

    public RotateRegisterRight(RegisterReader source, RegisterWriter target, CarryTransfer carryTransfer) {
        this.source = source;
        this.target = target;
        this.carryTransfer = carryTransfer;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int addOldCarry = carryTransfer.resolveCarry(flags, n);

        int result = ((n >> 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b0000_0001) != 0;

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

    private static int carryFromBit0(Flags flags, int n) {
        return (n & 0b0000_0001);
    }


    public static Instruction A() {
        return new RotateRegisterRight(Registers::readA, Registers::writeA, RotateRegisterRight::carryFromBit0); //TODO: fix this having 4 cycles without anonymous class
    }

    public static Instruction B() {
        return new RotateRegisterRight(Registers::readB, Registers::writeB, RotateRegisterRight::carryFromBit0);
    }

    public static Instruction C() {
        return new RotateRegisterRight(Registers::readC, Registers::writeC, RotateRegisterRight::carryFromBit0);
    }

    public static Instruction D() {
        return new RotateRegisterRight(Registers::readD, Registers::writeD, RotateRegisterRight::carryFromBit0);
    }

    public static Instruction E() {
        return new RotateRegisterRight(Registers::readE, Registers::writeE, RotateRegisterRight::carryFromBit0);
    }

    public static Instruction H() {
        return new RotateRegisterRight(Registers::readH, Registers::writeH, RotateRegisterRight::carryFromBit0);
    }

    public static Instruction L() {
        return new RotateRegisterRight(Registers::readL, Registers::writeL, RotateRegisterRight::carryFromBit0);
    }

    public static Instruction AthroughCarry() {
        return new RotateRegisterRight(Registers::readE, Registers::writeE, RotateRegisterRight::carryFromFlags);
    }

    private interface CarryTransfer {
        int resolveCarry(Flags flags, int n);
    }
}
