package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryWriteInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAIntoAddressOfWord implements MemoryWriteInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = programCounter.wordOperand(memory);
        memory.writeByte(address, registers.readA());

        return totalCycles();
    }

    public int totalCycles() {
        return 16;
    }
}
