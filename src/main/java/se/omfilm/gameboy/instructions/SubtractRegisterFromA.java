package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.Function;

public class SubtractRegisterFromA implements Instruction {
    private final Function<Registers, Integer> reader;

    private SubtractRegisterFromA(Function<Registers, Integer> reader) {
        this.reader = reader;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = reader.apply(registers);
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
