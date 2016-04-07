package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAIntoByteOffsetByte implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = programCounter.byteOperand(memory);
        memory.writeByte(Memory.MemoryType.IO_REGISTERS.from + data, registers.readA());

        return 12;
    }
}
