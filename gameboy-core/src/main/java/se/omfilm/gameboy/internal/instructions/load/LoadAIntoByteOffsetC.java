package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryWriteInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAIntoByteOffsetC implements MemoryWriteInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        memory.writeByte(MMU.MemoryType.IO_REGISTERS.from + registers.readC(), registers.readA());

        return totalCycles();
    }

    public int totalCycles() {
        return 8;
    }
}
