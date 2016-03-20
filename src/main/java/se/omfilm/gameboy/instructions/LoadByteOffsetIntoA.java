package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadByteOffsetIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(programCounter.increase());
        int address = Memory.MemoryType.IO_REGISTERS.from + n;
        registers.writeA(memory.readByte(address));

        return 12;
    }
}
