package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadWordIntoSP implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.write(memory.readWord(programCounter.increase(2)));

        return 8;
    }
}
