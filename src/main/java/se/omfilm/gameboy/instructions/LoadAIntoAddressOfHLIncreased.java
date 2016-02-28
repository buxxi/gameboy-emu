package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAIntoAddressOfHLIncreased implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int hl = registers.readHL();
        memory.writeByte(hl, registers.readA());
        registers.writeHL(hl + 1);

        return 8;
    }
}
