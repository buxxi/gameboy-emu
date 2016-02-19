package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;

public class LoadWordIntoRegister implements Instruction {
    private BiConsumer<Registers, Integer> apply;

    public LoadWordIntoRegister(BiConsumer<Registers, Integer> apply) {
        this.apply = apply;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        apply.accept(registers, memory.readWord(programCounter.increase(2)));
    }

    public static Instruction toDE() {
        return new LoadWordIntoRegister(Registers::writeDE);
    }

    public static Instruction toHL() {
        return new LoadWordIntoRegister(Registers::writeHL);
    }
}
