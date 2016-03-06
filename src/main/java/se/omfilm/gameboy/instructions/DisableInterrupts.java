package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class DisableInterrupts implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        flags.setInterruptsDisabled(true);
        return 4;
    }
}
