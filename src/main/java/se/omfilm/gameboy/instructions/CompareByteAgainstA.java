package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CompareByteAgainstA implements Instruction {
    @Override
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        int a = registers.readA();

        boolean zero = n == a;
        boolean carry = n > a;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }
}
