package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DecrementByteRegister implements Instruction {
    private final Function<Registers, Integer> reader;
    private final BiConsumer<Registers, Integer> writer;

    public DecrementByteRegister(Function<Registers, Integer> reader, BiConsumer<Registers, Integer> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int value = reader.apply(registers);
        boolean halfCarry = (value & 0x0F) == 0;
        value = (value - 1) & 0xFF;
        boolean zero = value == 0;

        flags.set(Flags.flags(halfCarry, zero));
        writer.accept(registers, value);
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
