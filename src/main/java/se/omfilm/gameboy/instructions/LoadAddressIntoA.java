package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

//TODO: better name
public class LoadAddressIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int nn = memory.readWord(programCounter.increase(2));
        registers.writeA(memory.readByte(nn));
        return 16;
    }
}
