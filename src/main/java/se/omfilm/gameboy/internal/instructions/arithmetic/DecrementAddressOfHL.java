package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class DecrementAddressOfHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int hl = registers.readHL();

        int n = memory.readByte(hl);
        boolean halfCarry = (n & 0x0F) == 0;
        n = (n - 1) & 0xFF;
        boolean zero = n == 0;

        memory.writeByte(hl, n);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);

        return 12;
    }
}
