package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class InvalidInstruction implements Instruction {
    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        throw new UnsupportedOperationException("Invalid instruction.");
    }
}
