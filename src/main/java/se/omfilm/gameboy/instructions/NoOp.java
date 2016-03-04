package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class NoOp implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        return 4;
    }
}
