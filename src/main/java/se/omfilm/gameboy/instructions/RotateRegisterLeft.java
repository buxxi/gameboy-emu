package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class RotateRegisterLeft implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public RotateRegisterLeft(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int addOldCarry = flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
        int n = source.read(registers);

        int result = ((n << 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b1000_0000) != 0;

        target.write(registers, result);
        flags.set(Flags.flags(zero, false, carry));
    }

    public static Instruction C() {
        return new RotateRegisterLeft(Registers::readC, Registers::writeC);
    }

    public static Instruction A() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA);
    }
}
