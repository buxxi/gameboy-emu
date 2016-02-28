package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadByteOffsetIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = 0xFF00 + memory.readByte(programCounter.increase());
        registers.writeA(memory.readByte(address));

        return 12;
    }
}
