package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadStackPointerToAddressOfWord implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = memory.readWord(programCounter.increase(2));
        memory.writeWord(address, stackPointer.read());
        return 20;
    }
}
