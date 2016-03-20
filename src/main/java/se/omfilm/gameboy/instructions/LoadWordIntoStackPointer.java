package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadWordIntoStackPointer implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.write(programCounter.wordOperand(memory));

        return 8;
    }
}
