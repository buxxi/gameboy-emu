package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadByteAddressOfDEIntoA implements Instruction {
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        registers.writeA(memory.readByte(registers.readDE()));
    }
}
