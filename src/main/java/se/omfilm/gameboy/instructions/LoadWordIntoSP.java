package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadWordIntoSP implements Instruction {
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.write(memory.readWord(programCounter.read()));
        programCounter.increase(2);
    }
}
