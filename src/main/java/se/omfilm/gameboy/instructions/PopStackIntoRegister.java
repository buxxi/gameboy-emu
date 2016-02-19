package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;

public class PopStackIntoRegister implements Instruction {
    private BiConsumer<Registers, Integer> apply;

    public PopStackIntoRegister(BiConsumer<Registers, Integer> apply) {
        this.apply = apply;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int pointer = stackPointer.read();
        apply.accept(registers, memory.readWord(pointer));
        stackPointer.write(pointer + 2);
    }

    public static Instruction toBC() {
        return new PopStackIntoRegister(Registers::writeBC);
    }
}
