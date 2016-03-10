package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAddressIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(programCounter.read());
        registers.writeA(n);
        return 16;
    }
}
