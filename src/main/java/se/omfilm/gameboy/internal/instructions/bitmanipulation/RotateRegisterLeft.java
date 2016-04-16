package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class RotateRegisterLeft implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;
    private final CarryTransfer carryTransfer;
    private final boolean resetZero;

    private RotateRegisterLeft(RegisterReader source, RegisterWriter target, CarryTransfer carryTransfer, boolean resetZero) {
        this.source = source;
        this.target = target;
        this.carryTransfer = carryTransfer;
        this.resetZero = resetZero;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int addOldCarry = carryTransfer.resolveCarry(flags, n);

        int result = ((n << 1) & 0xFF) | addOldCarry;

        boolean zero = result == 0 && !resetZero;
        boolean carry = (n & 0b1000_0000) != 0;

        target.write(registers, result);

        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.HALF_CARRY);
        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.CARRY, carry);

        return 4;
    }

    private static int carryFromFlags(Flags flags, int n) {
        return flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
    }

    private static int carryFromBit7(Flags flags, int n) {
        return n >> 7;
    }

    public static Instruction AresetZero() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA, RotateRegisterLeft::carryFromBit7, true);
    }

    public static Instruction A() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction B() {
        return new RotateRegisterLeft(Registers::readB, Registers::writeB, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction C() {
        return new RotateRegisterLeft(Registers::readC, Registers::writeC, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction D() {
        return new RotateRegisterLeft(Registers::readD, Registers::writeD, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction E() {
        return new RotateRegisterLeft(Registers::readE, Registers::writeE, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction H() {
        return new RotateRegisterLeft(Registers::readH, Registers::writeH, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction L() {
        return new RotateRegisterLeft(Registers::readL, Registers::writeL, RotateRegisterLeft::carryFromBit7, false);
    }

    public static Instruction AthroughCarry() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA, RotateRegisterLeft::carryFromFlags, false);
    }

    public static Instruction AthroughCarryResetZero() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA, RotateRegisterLeft::carryFromFlags, true);
    }

    public static Instruction BthroughCarry() {
        return new RotateRegisterLeft(Registers::readB, Registers::writeB, RotateRegisterLeft::carryFromFlags, false);
    }

    public static Instruction CthroughCarry() {
        return new RotateRegisterLeft(Registers::readC, Registers::writeC, RotateRegisterLeft::carryFromFlags, false);
    }

    public static Instruction DthroughCarry() {
        return new RotateRegisterLeft(Registers::readD, Registers::writeD, RotateRegisterLeft::carryFromFlags, false);
    }

    public static Instruction EthroughCarry() {
        return new RotateRegisterLeft(Registers::readE, Registers::writeE, RotateRegisterLeft::carryFromFlags, false);
    }

    public static Instruction HthroughCarry() {
        return new RotateRegisterLeft(Registers::readH, Registers::writeH, RotateRegisterLeft::carryFromFlags, false);
    }

    public static Instruction LthroughCarry() {
        return new RotateRegisterLeft(Registers::readL, Registers::writeL, RotateRegisterLeft::carryFromFlags, false);
    }

    private interface CarryTransfer {
        int resolveCarry(Flags flags, int n);
    }
}
