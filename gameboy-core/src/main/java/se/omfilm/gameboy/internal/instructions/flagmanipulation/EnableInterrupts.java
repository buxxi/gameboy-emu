package se.omfilm.gameboy.internal.instructions.flagmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class EnableInterrupts implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        flags.setInterruptsDisabled(false);
        return 4;
    }
}
