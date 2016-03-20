package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAddressOfByteIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = programCounter.wordOperand(memory);
        registers.writeA(memory.readByte(address));
        return 16;
    }
}
