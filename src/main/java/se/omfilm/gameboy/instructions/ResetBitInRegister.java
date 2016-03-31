package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ResetBitInRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;
    private final int bit;

    private ResetBitInRegister(RegisterReader source, RegisterWriter target, int bit) {
        this.source = source;
        this.target = target;
        this.bit = bit;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int value = source.read(registers);
        target.write(registers, value & (~(1 << bit)));
        return 8;
    }

    public static Instruction bit0ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 0);
    }

    public static Instruction bit1ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 1);
    }

    public static Instruction bit2ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 2);
    }

    public static Instruction bit3ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 3);
    }

    public static Instruction bit4ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 4);
    }

    public static Instruction bit5ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 5);
    }

    public static Instruction bit6ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 6);
    }

    public static Instruction bit0ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 0);
    }

    public static Instruction bit1ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 1);
    }

    public static Instruction bit2ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 2);
    }

    public static Instruction bit3ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 3);
    }

    public static Instruction bit4ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 4);
    }

    public static Instruction bit5ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 5);
    }

    public static Instruction bit6ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 6);
    }

    public static Instruction bit7ofB() {
        return new ResetBitInRegister(Registers::readB, Registers::writeB, 7);
    }

    public static Instruction bit0ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 0);
    }

    public static Instruction bit1ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 1);
    }

    public static Instruction bit2ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 2);
    }

    public static Instruction bit3ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 3);
    }

    public static Instruction bit4ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 4);
    }

    public static Instruction bit5ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 5);
    }

    public static Instruction bit6ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 6);
    }

    public static Instruction bit7ofC() {
        return new ResetBitInRegister(Registers::readC, Registers::writeC, 7);
    }

    public static Instruction bit0ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 0);
    }

    public static Instruction bit1ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 1);
    }

    public static Instruction bit2ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 2);
    }

    public static Instruction bit3ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 3);
    }

    public static Instruction bit4ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 4);
    }

    public static Instruction bit5ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 5);
    }

    public static Instruction bit6ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 6);
    }

    public static Instruction bit7ofD() {
        return new ResetBitInRegister(Registers::readD, Registers::writeD, 7);
    }

    public static Instruction bit0ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 0);
    }

    public static Instruction bit1ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 1);
    }

    public static Instruction bit2ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 2);
    }

    public static Instruction bit3ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 3);
    }

    public static Instruction bit4ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 4);
    }

    public static Instruction bit5ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 5);
    }

    public static Instruction bit6ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 6);
    }

    public static Instruction bit7ofE() {
        return new ResetBitInRegister(Registers::readE, Registers::writeE, 7);
    }

    public static Instruction bit0ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 0);
    }

    public static Instruction bit1ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 1);
    }

    public static Instruction bit2ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 2);
    }

    public static Instruction bit3ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 3);
    }

    public static Instruction bit4ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 4);
    }

    public static Instruction bit5ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 5);
    }

    public static Instruction bit6ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 6);
    }

    public static Instruction bit7ofH() {
        return new ResetBitInRegister(Registers::readH, Registers::writeH, 7);
    }

    public static Instruction bit7ofA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 7);
    }

    public static Instruction bit0ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 0);
    }

    public static Instruction bit1ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 1);
    }

    public static Instruction bit2ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 2);
    }

    public static Instruction bit3ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 3);
    }

    public static Instruction bit4ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 4);
    }

    public static Instruction bit5ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 5);
    }

    public static Instruction bit6ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 6);
    }

    public static Instruction bit7ofL() {
        return new ResetBitInRegister(Registers::readL, Registers::writeL, 7);
    }
}
