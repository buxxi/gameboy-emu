package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class XorByteWithA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = memory.readByte(programCounter.increase());
        int result = (a ^ n) & 0xFF;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.HALF_CARRY, Flags.Flag.CARRY);

        return 8;
    }
}
