package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ComplementCarryFlag implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        flags.set(Flags.Flag.CARRY, !flags.isSet(Flags.Flag.CARRY));
        flags.reset(Flags.Flag.HALF_CARRY, Flags.Flag.SUBTRACT);

        return 4;
    }
}
