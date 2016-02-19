package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class RotateRegisterLeft implements Instruction {
    private final Function<Registers, Integer> reader;
    private final BiConsumer<Registers, Integer> writer;

    public RotateRegisterLeft(Function<Registers, Integer> reader, BiConsumer<Registers, Integer> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int carry = flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
        int value = reader.apply(registers);

        if ((value & 0b10000000) == 1) {
            flags.set(Flags.Flag.CARRY);
        } else {
            flags.set();
        }

        writer.accept(registers, ((value << 1) + carry) & 0xFF);
    }

    public static Instruction C() {
        return new RotateRegisterLeft(Registers::readC, Registers::writeC);
    }

    public static Instruction A() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA);
    }
}
