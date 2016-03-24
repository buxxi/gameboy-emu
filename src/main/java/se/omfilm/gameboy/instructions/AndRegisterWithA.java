package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AndRegisterWithA implements Instruction {
    private final RegisterReader source;

    public AndRegisterWithA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = source.read(registers);
        int result = a & n;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.set(Flags.Flag.HALF_CARRY, true);
        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.CARRY);

        return 4;
    }

    public static Instruction A() {
        return new AndRegisterWithA(Registers::readA);
    }

    public static Instruction B() {
        return new AndRegisterWithA(Registers::readB);
    }

    public static Instruction C() {
        return new AndRegisterWithA(Registers::readC);
    }

    public static Instruction D() {
        return new AndRegisterWithA(Registers::readD);
    }

    public static Instruction E() {
        return new AndRegisterWithA(Registers::readE);
    }

    public static Instruction H() {
        return new AndRegisterWithA(Registers::readH);
    }

    public static Instruction L() {
        return new AndRegisterWithA(Registers::readL);
    }
}
