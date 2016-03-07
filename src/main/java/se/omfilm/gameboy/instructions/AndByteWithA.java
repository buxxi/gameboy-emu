package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AndByteWithA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(programCounter.increase());
        int result = n & registers.readA();
        registers.writeA(result);

        boolean zero = n == 0;

        flags.set(Flags.flags(zero, true, false));

        return 8;
    }
}
