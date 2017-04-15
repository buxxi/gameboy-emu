package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class SubtractRegisterFromA implements Instruction {
    private final RegisterReader source;

    private SubtractRegisterFromA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readA();

        int result = (a - n) & 0xFF;
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
        return new SubtractRegisterFromA(Registers::readA);
    }

    public static Instruction fromB() {
        return new SubtractRegisterFromA(Registers::readB);
    }

    public static Instruction fromC() {
        return new SubtractRegisterFromA(Registers::readC);
    }

    public static Instruction fromD() {
        return new SubtractRegisterFromA(Registers::readD);
    }

    public static Instruction fromE() {
        return new SubtractRegisterFromA(Registers::readE);
    }

    public static Instruction fromH() {
        return new SubtractRegisterFromA(Registers::readH);
    }

    public static Instruction fromL() {
        return new SubtractRegisterFromA(Registers::readL);
    }
}
