package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class SwapAddressOfHL implements Instruction {

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int address = registers.readHL();
        int n = memory.readByte(address);
        int result = ((n & 0b0000_1111) << 4) | ((n & 0b1111_0000) >> 4);

        memory.writeByte(address, result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.HALF_CARRY, Flags.Flag.CARRY);

        return 16;
    }
}
