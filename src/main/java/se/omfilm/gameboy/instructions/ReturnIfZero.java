package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ReturnIfZero extends Return {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        if (flags.isSet(Flags.Flag.ZERO)) {
            super.execute(memory, registers, flags, programCounter, stackPointer);
        }

        return 8;
    }
}
