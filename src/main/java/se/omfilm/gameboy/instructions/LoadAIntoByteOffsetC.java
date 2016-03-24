package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAIntoByteOffsetC implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        memory.writeByte(Memory.MemoryType.IO_REGISTERS.from + registers.readC(), registers.readA());

        return 8;
    }
}
