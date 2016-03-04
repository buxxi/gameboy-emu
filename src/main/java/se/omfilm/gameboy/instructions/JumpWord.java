package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class JumpWord implements Instruction {
    @Override
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int nn = memory.readWord(programCounter.increase());
        programCounter.write(nn);
        return 12;
    }
}
