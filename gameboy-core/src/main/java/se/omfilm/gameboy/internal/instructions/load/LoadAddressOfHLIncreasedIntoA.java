package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryReadInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAddressOfHLIncreasedIntoA implements MemoryReadInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int hl = registers.readHL();
        registers.writeA(memory.readByte(hl));
        registers.writeHL((hl + 1) & 0xFFFF);
        return totalCycles();
    }

    public int totalCycles() {
        return 8;
    }
}
