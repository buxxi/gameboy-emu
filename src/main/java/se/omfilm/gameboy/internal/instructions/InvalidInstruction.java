package se.omfilm.gameboy.internal.instructions;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class InvalidInstruction implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        throw new UnsupportedOperationException("Invalid instruction.");
    }
}
