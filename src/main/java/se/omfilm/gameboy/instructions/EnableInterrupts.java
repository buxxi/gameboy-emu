package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class EnableInterrupts implements DelayedInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        return 4;
    }

    public boolean disableInterrupts() {
        return false;
    }
}
