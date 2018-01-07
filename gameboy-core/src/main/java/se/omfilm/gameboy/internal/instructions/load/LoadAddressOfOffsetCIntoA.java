package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryReadInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAddressOfOffsetCIntoA implements MemoryReadInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = MMU.MemoryType.IO_REGISTERS.from + registers.readC();
        registers.writeA(memory.readByte(address));
        return totalCycles();
    }

    public int totalCycles() {
        return 8;
    }
}
