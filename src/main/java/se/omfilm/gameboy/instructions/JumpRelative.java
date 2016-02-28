package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class JumpRelative implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = memory.readByte(programCounter.increase());
        programCounter.increase((byte) data);

        return 8;
    }
}
