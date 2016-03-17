package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadHLIntoStackPointer implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.write(registers.readHL());
        return 8;
    }
}
