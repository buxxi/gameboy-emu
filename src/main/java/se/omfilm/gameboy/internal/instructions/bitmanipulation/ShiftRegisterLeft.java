package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class ShiftRegisterLeft implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    private ShiftRegisterLeft(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int result = (n << 1) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b1000_0000) != 0;

        target.write(registers, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    public static Instruction A() {
        return new ShiftRegisterLeft(Registers::readA, Registers::writeA);
    }

    public static Instruction B() {
        return new ShiftRegisterLeft(Registers::readB, Registers::writeB);
    }

    public static Instruction C() {
        return new ShiftRegisterLeft(Registers::readC, Registers::writeC);
    }

    public static Instruction D() {
        return new ShiftRegisterLeft(Registers::readD, Registers::writeD);
    }

    public static Instruction E() {
        return new ShiftRegisterLeft(Registers::readE, Registers::writeE);
    }

    public static Instruction H() {
        return new ShiftRegisterLeft(Registers::readH, Registers::writeH);
    }

    public static Instruction L() {
        return new ShiftRegisterLeft(Registers::readL, Registers::writeL);
    }
}
