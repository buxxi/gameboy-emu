package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadRegisterIntoRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public LoadRegisterIntoRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, source.read(registers));

        return 4;
    }

    public static Instruction fromBToA() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeA);
    }

    public static Instruction fromCToA() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeA);
    }

    public static Instruction fromEToA() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeA);
    }

    public static Instruction fromHToA() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeA);
    }

    public static Instruction fromLToA() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeA);
    }

    public static Instruction fromAToB() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeB);
    }

    public static Instruction fromAToC() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeC);
    }

    public static Instruction fromAToD() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeD);
    }

    public static Instruction fromAToE() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeE);
    }

    public static Instruction fromAToH() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeH);
    }
}
