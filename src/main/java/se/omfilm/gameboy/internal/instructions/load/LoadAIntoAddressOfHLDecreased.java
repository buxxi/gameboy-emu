package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAIntoAddressOfHLDecreased implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int hl = registers.readHL();
        memory.writeByte(hl, registers.readA());
        registers.writeHL((hl - 1) & 0xFFFF);

        return 8;
    }
}
