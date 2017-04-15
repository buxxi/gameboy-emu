package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadByteIntoAddressOfHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        memory.writeByte(registers.readHL(), n);
        return 12;
    }
}
