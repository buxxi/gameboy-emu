package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryWriteInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadByteIntoAddressOfHL implements MemoryWriteInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        memory.writeByte(registers.readHL(), n);
        return totalCycles();
    }

    public int totalCycles() {
        return 12;
    }
}
