package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class XorA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int val = (a ^ a) & 0xFF;
        registers.writeA(val);
        if (val == 0) {
            flags.set(Flags.Flag.ZERO);
        } else {
            flags.set();
        }

        return 4;
    }
}
