package se.omfilm.gameboy.internal.instructions.flagmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class ComplementCarryFlag implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        flags.set(Flags.Flag.CARRY, !flags.isSet(Flags.Flag.CARRY));
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.SUBTRACT, false);

        return 4;
    }
}
