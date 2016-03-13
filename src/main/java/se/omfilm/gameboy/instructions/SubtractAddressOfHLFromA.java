package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class SubtractAddressOfHLFromA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(registers.readHL());
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

        return 8;
    }
}
