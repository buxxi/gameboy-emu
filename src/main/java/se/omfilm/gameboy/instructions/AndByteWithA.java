package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AndByteWithA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        int a = registers.readA();
        int result = n & a;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.set(Flags.Flag.HALF_CARRY, true);
        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.CARRY);

        return 8;
    }
}
