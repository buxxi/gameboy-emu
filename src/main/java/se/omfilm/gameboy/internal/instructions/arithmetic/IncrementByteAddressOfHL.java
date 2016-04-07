package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class IncrementByteAddressOfHL implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        int result = (n + 1) & 0xFF;

        boolean zero = result == 0;
        boolean halfCarry = (result & 0x0F) == 0;

        memory.writeByte(address, result);

        flags.reset(Flags.Flag.SUBTRACT);
        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);

        return 12;
    }
}
