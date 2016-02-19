package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;

public class LoadByteIntoRegister implements Instruction {
    private BiConsumer<Registers, Integer> apply;

    private LoadByteIntoRegister(BiConsumer<Registers, Integer> apply) {
        this.apply = apply;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        apply.accept(registers, memory.readByte(programCounter.increase()));
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

    public static Instruction toL() {
        return new LoadByteIntoRegister(Registers::writeL);
    }
}
