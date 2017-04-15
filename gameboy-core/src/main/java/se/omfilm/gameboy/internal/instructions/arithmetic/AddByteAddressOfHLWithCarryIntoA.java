package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class AddByteAddressOfHLWithCarryIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(registers.readHL());
        int a = registers.readA();
        int result = n + a + (flags.isSet(Flags.Flag.CARRY) ? 1 : 0);

        boolean carry = result > 0xFF;
        result = result & 0xFF;
        boolean halfCarry = ((result ^ a ^ n) & 0x10) != 0;
        boolean zero = result == 0;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }
}
