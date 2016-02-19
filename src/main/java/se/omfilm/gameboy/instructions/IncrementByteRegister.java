package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class IncrementByteRegister implements Instruction {
    private final Function<Registers, Integer> reader;
    private final BiConsumer<Registers, Integer> writer;

    public IncrementByteRegister(Function<Registers, Integer> reader, BiConsumer<Registers, Integer> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = reader.apply(registers);
        int result = (n + 1) % 0xFF;
        writer.accept(registers, result);

        boolean zero = result == 0;
        boolean halfCarry = (n & 0x0F) == 0x0F;

        flags.set(Flags.flags(zero, halfCarry, false));
    }

    public static Instruction C() {
        return new IncrementByteRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction B() {
        return new IncrementByteRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction H() {
        return new IncrementByteRegister(Registers::readH, Registers::writeH);
    }
}
