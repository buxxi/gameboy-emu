package se.omfilm.gameboy.internal.instructions.flagmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class CompareByteAgainstA implements Instruction {
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
