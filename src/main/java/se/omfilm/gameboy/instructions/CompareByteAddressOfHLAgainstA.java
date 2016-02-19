package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CompareByteAddressOfHLAgainstA implements Instruction {
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(registers.readHL());
        int a = registers.readA();

        boolean zero = n == a;
        boolean carry = a < n;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);

        flags.set(Flags.withNegative(zero, halfCarry, carry));
    }
}
