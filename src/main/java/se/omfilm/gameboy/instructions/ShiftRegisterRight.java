package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ShiftRegisterRight implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    private ShiftRegisterRight(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int result = n >> 1;

        boolean zero = result == 0;
        boolean carry = (n & 0b0000_0001) != 0;

        target.write(registers, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    public static Instruction A() {
        return new ShiftRegisterRight(Registers::readA, Registers::writeA);
    }

    public static Instruction B() {
        return new ShiftRegisterRight(Registers::readB, Registers::writeB);
    }
}
