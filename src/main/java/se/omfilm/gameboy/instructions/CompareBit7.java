package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CompareBit7 implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        boolean isSet = (registers.readH() & 0b1000_0000) != 0;

        flags.set(Flags.Flag.ZERO, !isSet);
        flags.reset(Flags.Flag.SUBTRACT);
        flags.set(Flags.Flag.HALF_CARRY, true);

        return 8;
    }
}
