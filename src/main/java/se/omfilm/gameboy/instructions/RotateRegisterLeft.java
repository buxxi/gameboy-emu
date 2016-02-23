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
        int addOldCarry = flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
        int n = reader.apply(registers);

        int result = ((n << 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b1000_0000) != 0;

        writer.accept(registers, result);
        flags.set(Flags.flags(zero, false, carry));
    }

    public static Instruction C() {
        return new RotateRegisterLeft(Registers::readC, Registers::writeC);
    }

    public static Instruction A() {
        return new RotateRegisterLeft(Registers::readA, Registers::writeA);
    }
}
