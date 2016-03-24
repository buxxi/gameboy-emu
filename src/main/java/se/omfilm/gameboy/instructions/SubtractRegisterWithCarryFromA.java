package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class SubtractRegisterWithCarryFromA implements Instruction {
    private final RegisterReader source;

    private SubtractRegisterWithCarryFromA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readA();

        int result = (a - n - (flags.isSet(Flags.Flag.CARRY) ? 1 : 0)) & 0xFF;
        boolean zero = result == 0;
        boolean carry = n > a;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);
        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 4;
    }

    public static Instruction fromA() {
        return new SubtractRegisterWithCarryFromA(Registers::readA);
    }

    public static Instruction fromB() {
        return new SubtractRegisterWithCarryFromA(Registers::readB);
    }

    public static Instruction fromC() {
        return new SubtractRegisterWithCarryFromA(Registers::readC);
    }

    public static Instruction fromD() {
        return new SubtractRegisterWithCarryFromA(Registers::readD);
    }

    public static Instruction fromE() {
        return new SubtractRegisterWithCarryFromA(Registers::readE);
    }

    public static Instruction fromH() {
        return new SubtractRegisterWithCarryFromA(Registers::readH);
    }

    public static Instruction fromL() {
        return new SubtractRegisterWithCarryFromA(Registers::readL);
    }
}
