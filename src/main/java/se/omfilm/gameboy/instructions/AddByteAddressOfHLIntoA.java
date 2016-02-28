package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AddByteAddressOfHLIntoA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(registers.readHL());
        int a = registers.readA();
        int result = n + a;

        boolean carry = result > 0xFF;
        result = result & 0xFF;
        boolean halfCarry = ((result ^ a ^ n) & 0x10) != 0;
        boolean zero = result == 0;

        registers.writeA(result);
        flags.set(Flags.flags(zero, halfCarry, carry));

        return 8;
    }
}
