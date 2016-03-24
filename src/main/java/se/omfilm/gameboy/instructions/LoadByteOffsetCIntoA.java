package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadByteOffsetCIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = Memory.MemoryType.IO_REGISTERS.from + registers.readC();
        registers.writeA(memory.readByte(address));
        return 8;
    }
}
