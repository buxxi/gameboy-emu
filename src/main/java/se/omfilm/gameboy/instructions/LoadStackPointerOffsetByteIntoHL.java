package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadStackPointerOffsetByteIntoHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        int sp = stackPointer.read();
        int result = (sp + ((byte) n)) & 0xFFFF;

        boolean carry = ((sp ^ n ^ result) & 0x100) != 0;
        boolean halfCarry = ((sp ^ n ^ result) & 0x10) != 0;

        registers.writeHL(result);

        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.ZERO);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 12;
    }
}
