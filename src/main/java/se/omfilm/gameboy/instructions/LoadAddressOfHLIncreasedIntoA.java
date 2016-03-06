package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAddressOfHLIncreasedIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int hl = registers.readHL();
        registers.writeA(memory.readByte(hl));
        registers.writeHL(hl + 1);
        return 8;
    }
}
