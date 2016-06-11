package se.omfilm.gameboy.internal.instructions.logic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class AndAddressOfHLWithA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = memory.readByte(registers.readHL());
        int result = a & n;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.set(Flags.Flag.HALF_CARRY, true);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.CARRY, false);

        return 8;
    }
}
