package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class EnableInterrupts implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        flags.setInterruptsDisabled(false);
        return 4;
    }
}
