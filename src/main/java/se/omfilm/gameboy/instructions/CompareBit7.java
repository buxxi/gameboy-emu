package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CompareBit7 implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        boolean isSet = ((registers.readH() & 0b0100000) != 0);
        if (isSet) {
            flags.set(Flags.Flag.HALF_CARRY);
        } else {
            flags.set(Flags.Flag.ZERO, Flags.Flag.HALF_CARRY);
        }

        return 8;
    }
}
