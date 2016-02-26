package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class SubtractRegisterFromA implements Instruction {
    private final RegisterReader source;

    private SubtractRegisterFromA(RegisterReader source) {
        this.source = source;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readA();

        int result = (a - n) & 0xFF;
        boolean zero = result == 0;
        boolean carry = n > a;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);
        registers.writeA(result);
        flags.set(Flags.withNegative(zero, halfCarry, carry));
    }


    public static Instruction fromB() {
        return new SubtractRegisterFromA(Registers::readB);
    }
}
