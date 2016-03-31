package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CompareBit implements Instruction {
    private final RegisterReader reader;
    private final int bit;

    private CompareBit(RegisterReader reader, int bit) {
        this.reader = reader;
        this.bit = bit;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        boolean isSet = (reader.read(registers) & (1 << bit)) != 0;

        flags.set(Flags.Flag.ZERO, !isSet);
        flags.reset(Flags.Flag.SUBTRACT);
        flags.set(Flags.Flag.HALF_CARRY, true);

        return 8;
    }

    public static Instruction bit0ofA() {
        return new CompareBit(Registers::readA, 0);
    }

    public static Instruction bit1ofA() {
        return new CompareBit(Registers::readA, 1);
    }

    public static Instruction bit2ofA() {
        return new CompareBit(Registers::readA, 2);
    }

    public static Instruction bit3ofA() {
        return new CompareBit(Registers::readA, 3);
    }

    public static Instruction bit4ofA() {
        return new CompareBit(Registers::readA, 4);
    }

    public static Instruction bit5ofA() {
        return new CompareBit(Registers::readA, 5);
    }

    public static Instruction bit6ofA() {
        return new CompareBit(Registers::readA, 6);
    }

    public static Instruction bit7ofA() {
        return new CompareBit(Registers::readA, 7);
    }

    public static Instruction bit0ofB() {
        return new CompareBit(Registers::readB, 0);
    }

    public static Instruction bit1ofB() {
        return new CompareBit(Registers::readB, 1);
    }

    public static Instruction bit2ofB() {
        return new CompareBit(Registers::readB, 2);
    }

    public static Instruction bit3ofB() {
        return new CompareBit(Registers::readB, 3);
    }

    public static Instruction bit4ofB() {
        return new CompareBit(Registers::readB, 4);
    }

    public static Instruction bit5ofB() {
        return new CompareBit(Registers::readB, 5);
    }

    public static Instruction bit6ofB() {
        return new CompareBit(Registers::readB, 6);
    }

    public static Instruction bit7ofB() {
        return new CompareBit(Registers::readB, 7);
    }

    public static Instruction bit0ofC() {
        return new CompareBit(Registers::readC, 0);
    }

    public static Instruction bit1ofC() {
        return new CompareBit(Registers::readC, 1);
    }

    public static Instruction bit2ofC() {
        return new CompareBit(Registers::readC, 2);
    }

    public static Instruction bit3ofC() {
        return new CompareBit(Registers::readC, 3);
    }

    public static Instruction bit4ofC() {
        return new CompareBit(Registers::readC, 4);
    }

    public static Instruction bit5ofC() {
        return new CompareBit(Registers::readC, 5);
    }

    public static Instruction bit6ofC() {
        return new CompareBit(Registers::readC, 6);
    }

    public static Instruction bit7ofC() {
        return new CompareBit(Registers::readC, 7);
    }

    public static Instruction bit0ofD() {
        return new CompareBit(Registers::readD, 0);
    }

    public static Instruction bit1ofD() {
        return new CompareBit(Registers::readD, 1);
    }

    public static Instruction bit2ofD() {
        return new CompareBit(Registers::readD, 2);
    }

    public static Instruction bit3ofD() {
        return new CompareBit(Registers::readD, 3);
    }

    public static Instruction bit4ofD() {
        return new CompareBit(Registers::readD, 4);
    }

    public static Instruction bit5ofD() {
        return new CompareBit(Registers::readD, 5);
    }

    public static Instruction bit6ofD() {
        return new CompareBit(Registers::readD, 6);
    }

    public static Instruction bit7ofD() {
        return new CompareBit(Registers::readD, 7);
    }

    public static Instruction bit0ofE() {
        return new CompareBit(Registers::readE, 0);
    }

    public static Instruction bit1ofE() {
        return new CompareBit(Registers::readE, 1);
    }

    public static Instruction bit2ofE() {
        return new CompareBit(Registers::readE, 2);
    }

    public static Instruction bit3ofE() {
        return new CompareBit(Registers::readE, 3);
    }

    public static Instruction bit4ofE() {
        return new CompareBit(Registers::readE, 4);
    }

    public static Instruction bit5ofE() {
        return new CompareBit(Registers::readE, 5);
    }

    public static Instruction bit6ofE() {
        return new CompareBit(Registers::readE, 6);
    }

    public static Instruction bit7ofE() {
        return new CompareBit(Registers::readE, 7);
    }

    public static Instruction bit0ofH() {
        return new CompareBit(Registers::readH, 0);
    }

    public static Instruction bit1ofH() {
        return new CompareBit(Registers::readH, 1);
    }

    public static Instruction bit2ofH() {
        return new CompareBit(Registers::readH, 2);
    }

    public static Instruction bit3ofH() {
        return new CompareBit(Registers::readH, 3);
    }

    public static Instruction bit4ofH() {
        return new CompareBit(Registers::readH, 4);
    }

    public static Instruction bit5ofH() {
        return new CompareBit(Registers::readH, 5);
    }

    public static Instruction bit6ofH() {
        return new CompareBit(Registers::readH, 6);
    }

    public static Instruction bit7ofH() {
        return new CompareBit(Registers::readH, 7);
    }

    public static Instruction bit0ofL() {
        return new CompareBit(Registers::readL, 0);
    }

    public static Instruction bit1ofL() {
        return new CompareBit(Registers::readL, 1);
    }

    public static Instruction bit2ofL() {
        return new CompareBit(Registers::readL, 2);
    }

    public static Instruction bit3ofL() {
        return new CompareBit(Registers::readL, 3);
    }

    public static Instruction bit4ofL() {
        return new CompareBit(Registers::readL, 4);
    }

    public static Instruction bit5ofL() {
        return new CompareBit(Registers::readL, 5);
    }

    public static Instruction bit6ofL() {
        return new CompareBit(Registers::readL, 6);
    }

    public static Instruction bit7ofL() {
        return new CompareBit(Registers::readL, 7);
    }
}
