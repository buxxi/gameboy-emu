package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryWriteInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAIntoByteOffsetByte implements MemoryWriteInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = programCounter.byteOperand(memory);
        memory.writeByte(MMU.MemoryType.IO_REGISTERS.from + data, registers.readA());

        return totalCycles();
    }

    public int totalCycles() {
        return 12;
    }
}
