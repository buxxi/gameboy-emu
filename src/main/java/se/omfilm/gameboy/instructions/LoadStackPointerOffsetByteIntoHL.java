package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadStackPointerOffsetByteIntoHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(programCounter.increase());
        int s = stackPointer.read();
        int result = s + ((byte) n);

        boolean carry = result > 0xFFFF;
        boolean halfCarry = ((result ^ s ^ n) & 0x1000) != 0;

        registers.writeHL(result & 0xFFFF);

        flags.reset(Flags.Flag.ZERO);
        flags.reset(Flags.Flag.SUBTRACT);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 12;
    }
}
