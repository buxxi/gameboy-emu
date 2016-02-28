package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAOffsetC implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        memory.writeByte(0xFF00 + registers.readC(), registers.readA());

        return 8;
    }
}
