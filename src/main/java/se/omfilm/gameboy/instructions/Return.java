package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class Return implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int pointer = stackPointer.read();
        programCounter.write(memory.readWord(pointer));
        stackPointer.write(pointer + 2);

        return 8;
    }
}
