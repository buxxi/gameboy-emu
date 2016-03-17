package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CompareRegisterAgainstA implements Instruction {
    private final RegisterReader source;

    private CompareRegisterAgainstA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readA();

        boolean zero = n == a;
        boolean carry = a < n;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    public static Instruction E() {
        return new CompareRegisterAgainstA(Registers::readE);
    }
}
