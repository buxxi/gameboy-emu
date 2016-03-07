package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ComplementA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        a = ~ a & 0xFF;
        registers.writeA(a);

        flags.set(Flags.flags(true, true, false));

        return 4;
    }
}
