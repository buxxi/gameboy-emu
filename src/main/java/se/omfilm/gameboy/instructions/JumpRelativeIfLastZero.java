package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class JumpRelativeIfLastZero implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = memory.readByte(programCounter.increase());
        if (flags.isSet(Flags.Flag.ZERO)) {
            programCounter.increase((byte) data);
        }

        return 8;
    }
}
