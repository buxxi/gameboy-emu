package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class IncrementWordRegister implements Instruction {
    private final Function<Registers, Integer> reader;
    private final BiConsumer<Registers, Integer> writer;

    public IncrementWordRegister(Function<Registers, Integer> reader, BiConsumer<Registers, Integer> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int val = (reader.apply(registers) + 1) % 0xFFFF;
        writer.accept(registers, val);
    }

    public static Instruction HL() {
        return new IncrementWordRegister(Registers::readHL, Registers::writeHL);
    }

    public static Instruction DE() {
        return new IncrementWordRegister(Registers::readDE, Registers::writeDE);
    }
}
