package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AddStackPointerIntoHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = stackPointer.read();
        int hl = registers.readHL();
        int result = n + hl;

        boolean carry = result > 0xFFFF;
        result = result & 0xFFFF;
        boolean halfCarry = ((result ^ hl ^ n) & 0x1000) != 0;

        registers.writeHL(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.reset(Flags.Flag.SUBTRACT);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

}
