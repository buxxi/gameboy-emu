package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AddRegisterIntoA implements Instruction {
    private final RegisterReader source;

    public AddRegisterIntoA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readA();
        int result = n + a;

        boolean carry = result > 0xFF;
        result = result & 0xFF;
        boolean halfCarry = ((result ^ a ^ n) & 0x10) != 0;
        boolean zero = result == 0;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 4;
    }

    public static Instruction A() {
        return new AddRegisterIntoA(Registers::readA);
    }

    public static Instruction C() {
        return new AddRegisterIntoA(Registers::readC);
    }
}
