package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class AddByteToStackPointer implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = (byte) programCounter.byteOperand(memory);
        int sp = stackPointer.read();
        int result = (sp + n) & 0xFFFF;

        boolean carry = ((sp ^ n ^ result) & 0x100) != 0;
        boolean halfCarry = ((sp ^ n ^ result) & 0x10) != 0;

        stackPointer.write(result);

        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.ZERO);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 16;
    }
}
