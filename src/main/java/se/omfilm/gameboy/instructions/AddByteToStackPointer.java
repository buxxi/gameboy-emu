package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AddByteToStackPointer implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        int sp = stackPointer.read();
        int result = n + sp;

        boolean carry = result > 0xFFFF;
        boolean halfCarry = ((result ^ sp ^ n) & 0x1000) != 0;

        stackPointer.write(result & 0xFFFF);

        flags.reset(Flags.Flag.ZERO);
        flags.reset(Flags.Flag.SUBTRACT);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 16;
    }
}
