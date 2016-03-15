package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadByteIntoRegister implements Instruction {
    private final RegisterWriter target;

    private LoadByteIntoRegister(RegisterWriter target) {
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, memory.readByte(programCounter.increase()));
        return 8;
    }

    public static Instruction toA() {
        return new LoadByteIntoRegister(Registers::writeA);
    }

    public static Instruction toB() {
        return new LoadByteIntoRegister(Registers::writeB);
    }

    public static Instruction toC() {
        return new LoadByteIntoRegister(Registers::writeC);
    }

    public static Instruction toD() {
        return new LoadByteIntoRegister(Registers::writeD);
    }

    public static Instruction toE() {
        return new LoadByteIntoRegister(Registers::writeE);
    }

    public static Instruction toH() {
        return new LoadByteIntoRegister(Registers::writeH);
    }

    public static Instruction toL() {
        return new LoadByteIntoRegister(Registers::writeL);
    }
}
