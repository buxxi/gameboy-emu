package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AndAddressOfHLWithA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = memory.readByte(registers.readHL());
        int result = a & n;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.set(Flags.Flag.HALF_CARRY, true);
        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.CARRY);

        return 8;
    }
}
