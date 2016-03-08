package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class JumpToValueOfHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        programCounter.write(registers.readHL());

        return 4;
    }
}
