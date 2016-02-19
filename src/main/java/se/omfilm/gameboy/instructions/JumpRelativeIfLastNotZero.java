package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class JumpRelativeIfLastNotZero implements Instruction {
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        //Should be signed and not unsigned
        int data = memory.readByte(programCounter.increase());
        if (!flags.isSet(Flags.Flag.ZERO)) {
            programCounter.increase((byte) data);
        }
    }
}
