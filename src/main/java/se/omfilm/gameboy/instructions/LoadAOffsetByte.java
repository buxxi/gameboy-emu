package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAOffsetByte implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = memory.readByte(programCounter.increase());
        memory.writeByte(Memory.MemoryType.IO_REGISTERS.from + data, registers.readA());

        return 12;
    }
}
