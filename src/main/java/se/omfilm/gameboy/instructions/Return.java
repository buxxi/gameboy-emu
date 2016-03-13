package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class Return implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        programCounter.write(stackPointer.pop(memory));

        return 8;
    }
}
