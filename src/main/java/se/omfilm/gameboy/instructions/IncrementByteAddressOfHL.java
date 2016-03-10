package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class IncrementByteAddressOfHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        memory.writeByte(address, n + 1);
        return 12;
    }
}
