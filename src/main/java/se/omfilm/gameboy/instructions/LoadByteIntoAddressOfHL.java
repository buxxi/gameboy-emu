package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadByteIntoAddressOfHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        memory.writeByte(registers.readHL(), n);
        return 12;
    }
}
