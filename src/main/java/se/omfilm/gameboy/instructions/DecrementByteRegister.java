package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class DecrementByteRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public DecrementByteRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int value = source.read(registers);
        boolean halfCarry = (value & 0x0F) == 0;
        value = (value - 1) & 0xFF;
        boolean zero = value == 0;

        flags.set(Flags.flags(halfCarry, zero));
        target.write(registers, value);
        return 4;
    }

    public static Instruction A() { return new DecrementByteRegister(Registers::readA, Registers::writeA); }

    public static Instruction B() {
        return new DecrementByteRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction C() {
        return new DecrementByteRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction D() {
        return new DecrementByteRegister(Registers::readD, Registers::writeD);
    }

    public static Instruction E() {
        return new DecrementByteRegister(Registers::readE, Registers::writeE);
    }
}
