package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryReadInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadByteOffsetIntoA implements MemoryReadInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        int address = MMU.MemoryType.IO_REGISTERS.from + n;
        registers.writeA(memory.readByte(address));

        return totalCycles();
    }

    public int totalCycles() {
        return 12;
    }
}
