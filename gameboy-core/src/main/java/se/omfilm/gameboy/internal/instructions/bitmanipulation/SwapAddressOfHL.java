package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryModifyInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class SwapAddressOfHL implements MemoryModifyInstruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        int result = ((n & 0b0000_1111) << 4) | ((n & 0b1111_0000) >> 4);

        memory.writeByte(address, result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, false);

        return 16;
    }
}
