package se.omfilm.gameboy.internal.instructions.flagmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryReadInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class CompareByteAddressOfHLAgainstA implements MemoryReadInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(registers.readHL());
        int a = registers.readA();

        boolean zero = n == a;
        boolean carry = n > a;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return totalCycles();
    }

    public int totalCycles() {
        return 8;
    }
}
