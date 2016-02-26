package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class LoadRegisterIntoRegister implements Instruction {
    private final Function<Registers, Integer> reader;
    private final BiConsumer<Registers, Integer> writer;

    public LoadRegisterIntoRegister(Function<Registers, Integer> reader, BiConsumer<Registers, Integer> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        writer.accept(registers, reader.apply(registers));
    }

    public static Instruction fromBToA() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeA);
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

    public static Instruction fromAToC() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeC);
    }

    public static Instruction fromAToD() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeD);
    }

    public static Instruction fromAToH() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeH);
    }
}
