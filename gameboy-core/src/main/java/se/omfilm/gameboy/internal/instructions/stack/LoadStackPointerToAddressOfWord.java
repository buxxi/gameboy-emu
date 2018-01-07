package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryWriteInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadStackPointerToAddressOfWord implements MemoryWriteInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = programCounter.wordOperand(memory);
        memory.writeWord(address, stackPointer.read());
        return totalCycles();
    }

    public int totalCycles() {
        return 20;
    }
}
